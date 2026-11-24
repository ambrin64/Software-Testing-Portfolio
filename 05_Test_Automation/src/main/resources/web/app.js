'use strict';

/* Online Examination System — browser client.
   All rules are enforced by the Java back end (com.oes.*); this file only displays them. */

const $ = (selector, root = document) => root.querySelector(selector);
const $$ = (selector, root = document) => [...root.querySelectorAll(selector)];
const LETTERS = 'ABCDEFGH';
const sleep = ms => new Promise(r => setTimeout(r, ms));
const esc = s => String(s ?? '').replace(/[&<>"']/g,
  c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
const num = n => (Number.isInteger(n) ? String(n) : n.toFixed(1));

const app = {
  token: sessionStorage.getItem('oes.token'),
  user: null,
  info: null,
  exam: null,
  current: 0,
  timer: null,
  deadline: 0,
  finishing: false,
  pendingViolation: null,
};

/* ------------------------------------------------------------------ API */

async function api(path, data) {
  const options = { method: data ? 'POST' : 'GET', headers: {} };
  if (app.token) options.headers['X-Token'] = app.token;
  if (data) {
    options.headers['Content-Type'] = 'application/x-www-form-urlencoded';
    options.body = new URLSearchParams(data).toString();
  }
  const res = await fetch('/api/' + path, options);
  const body = await res.json().catch(() => ({}));
  if (!res.ok) {
    const err = new Error(body.error || res.statusText);
    err.status = res.status;
    if (res.status === 401 && app.user) signedOut('Your session has ended. Please sign in again.');
    throw err;
  }
  return body;
}

/* ------------------------------------------------------------------ UI helpers */

function show(view) {
  $$('.view').forEach(v => { v.hidden = v.id !== 'view-' + view; });
  document.body.dataset.view = view;
  window.scrollTo(0, 0);
}

function setAlert(el, kind, text, code, extraHtml = '') {
  el.className = 'alert alert-' + kind;
  el.innerHTML = `<span>${esc(text)}</span>`
    + (code ? `<code class="code-chip" title="Value returned by the Java service">${esc(code)}</code>` : '')
    + extraHtml;
  el.hidden = false;
}

function clearAlert(el) {
  el.hidden = true;
  el.innerHTML = '';
}

function toast(text, kind = 'info', ms = 4200) {
  const t = document.createElement('div');
  t.className = 'toast toast-' + kind;
  t.textContent = text;
  $('#toasts').append(t);
  requestAnimationFrame(() => t.classList.add('in'));
  setTimeout(() => { t.classList.remove('in'); setTimeout(() => t.remove(), 300); }, ms);
}

function modal({ title, html, confirm = 'OK', cancel = 'Cancel', tone = 'primary' }) {
  return new Promise(resolve => {
    $('#modal-title').textContent = title;
    $('#modal-body').innerHTML = html;
    const ok = $('#modal-ok');
    const no = $('#modal-cancel');
    ok.textContent = confirm;
    ok.className = 'btn btn-' + tone;
    no.hidden = !cancel;
    no.textContent = cancel || '';
    $('#modal').hidden = false;
    ok.focus();
    const done = value => { $('#modal').hidden = true; ok.onclick = no.onclick = null; resolve(value); };
    ok.onclick = () => done(true);
    no.onclick = () => done(false);
  });
}

function initials(name) {
  return name.split(/\s+/).filter(Boolean).slice(0, 2).map(w => w[0].toUpperCase()).join('');
}

function setUser(user) {
  app.user = user;
  $('#userbox').hidden = !user;
  if (user) {
    $('#user-name').textContent = user.name;
    $('#user-email').textContent = user.email;
    $('#user-initials').textContent = initials(user.name);
  }
}

function signedOut(message) {
  stopTimer();
  app.token = null;
  app.exam = null;
  sessionStorage.removeItem('oes.token');
  setUser(null);
  switchTab('login');
  if (message) setAlert($('#login-alert'), 'warn', message);
  show('auth');
}

/* ------------------------------------------------------------------ sign in / register */

function switchTab(tab) {
  $$('.tab').forEach(b => b.classList.toggle('active', b.dataset.tab === tab));
  $('#login-form').hidden = tab !== 'login';
  $('#register-form').hidden = tab !== 'register';
}
$$('.tab').forEach(b => b.addEventListener('click', () => switchTab(b.dataset.tab)));

function updatePasswordRules() {
  const p = $('#reg-password').value;
  const rules = { len: p.length >= 8 && p.length <= 16, upper: /[A-Z]/.test(p), digit: /\d/.test(p) };
  $$('#pw-rules li').forEach(li => li.classList.toggle('ok', rules[li.dataset.rule]));
}
$('#reg-password').addEventListener('input', updatePasswordRules);

$('#fill-demo').addEventListener('click', () => {
  const f = $('#login-form');
  f.elements.email.value = 'demo@oes.edu';
  f.elements.password.value = 'Exam2026';
});

$('#register-form').addEventListener('submit', async e => {
  e.preventDefault();
  const f = e.target;
  const alertEl = $('#register-alert');
  const button = f.querySelector('[type=submit]');
  const field = n => f.elements[n].value;
  button.disabled = true;
  try {
    const r = await api('register', {
      name: field('name'), email: field('email'), age: field('age'), password: field('password'),
    });
    if (r.status === 'SUCCESS') {
      const email = field('email');
      f.reset();
      updatePasswordRules();
      clearAlert(alertEl);
      switchTab('login');
      const login = $('#login-form');
      login.elements.email.value = email;
      login.elements.password.focus();
      setAlert($('#login-alert'), 'ok', r.message, r.status);
    } else {
      setAlert(alertEl, 'bad', r.message, r.status);
    }
  } catch (err) {
    setAlert(alertEl, 'bad', err.message);
  } finally {
    button.disabled = false;
  }
});

$('#login-form').addEventListener('submit', async e => {
  e.preventDefault();
  const f = e.target;
  const alertEl = $('#login-alert');
  const button = f.querySelector('[type=submit]');
  const email = f.elements.email.value;
  button.disabled = true;
  try {
    const r = await api('login', { email, password: f.elements.password.value });
    if (r.result === 'SUCCESS') {
      app.token = r.token;
      sessionStorage.setItem('oes.token', r.token);
      setUser(r.user);
      f.reset();
      clearAlert(alertEl);
      await enterDashboard();
    } else if (r.result === 'ACCOUNT_LOCKED') {
      setAlert(alertEl, 'bad', r.message, r.result,
        `<button class="link-btn" type="button" data-unlock="${esc(email)}">Unlock as administrator</button>`);
    } else {
      setAlert(alertEl, r.result === 'INVALID_CREDENTIALS' ? 'warn' : 'bad', r.message, r.result);
    }
  } catch (err) {
    setAlert(alertEl, 'bad', err.message);
  } finally {
    button.disabled = false;
  }
});

$('#login-alert').addEventListener('click', async e => {
  const b = e.target.closest('[data-unlock]');
  if (!b) return;
  try {
    const r = await api('admin/unlock', { email: b.dataset.unlock });
    setAlert($('#login-alert'), 'ok', r.message, r.result);
  } catch (err) {
    setAlert($('#login-alert'), 'bad', err.message);
  }
});

$('#logout-btn').addEventListener('click', async () => {
  if (document.body.dataset.view === 'exam') {
    const ok = await modal({
      title: 'Leave the examination?',
      html: '<p>Your exam is still running and the timer will not stop. You can sign in again to resume.</p>',
      confirm: 'Sign out', tone: 'danger',
    });
    if (!ok) return;
  }
  try { await api('logout', {}); } catch { /* ignore */ }
  signedOut();
});

/* ------------------------------------------------------------------ dashboard */

function greeting() {
  const h = new Date().getHours();
  return h < 12 ? 'Good morning' : h < 17 ? 'Good afternoon' : 'Good evening';
}

async function enterDashboard() {
  const [info, me] = await Promise.all([app.info ? app.info : api('exam/info'), api('me')]);
  app.info = info;
  $('#greeting').textContent = greeting();
  $('#dash-name').textContent = app.user.name.split(/\s+/)[0];
  $('#dash-title').textContent = info.title;
  $('#dash-sections').textContent = info.sections
    .map(s => `Section ${s.id}: ${s.title} (${s.questions})`).join('  ·  ');
  $('#fact-questions').textContent = info.questions;
  $('#fact-duration').textContent = formatDuration(info.durationSeconds);
  $('#fact-marks').textContent = num(info.maxScore);
  $('#fact-pass').textContent = num(info.passPercentage) + '%';
  $('#rules').innerHTML = [
    `Each correct answer earns <strong>+${num(info.marksCorrect)}</strong>; each wrong answer costs <strong>−${num(info.marksWrong)}</strong>; unanswered questions score 0. The total never goes below zero.`,
    `To <strong>pass</strong> you need at least <strong>${num(info.passPercentage)}%</strong> and must attempt <strong>every section</strong>.`,
    `You may change an answer at any time before submitting. After submission, answers are <strong>final</strong>.`,
    `When the timer reaches zero the paper <strong>closes automatically</strong> and is graded as it stands.`,
    `Leaving the exam window is recorded. After <strong>${info.maxViolations} warnings</strong> the attempt is marked as malpractice and the result is <strong>withheld</strong>.`,
  ].map(r => `<li>${r}</li>`).join('');
  const last = me.exam && me.exam.state;
  $('#last-result-btn').hidden = !(last === 'SUBMITTED' || last === 'TIMED_OUT');
  show('dashboard');
}

function formatDuration(seconds) {
  return seconds % 60 === 0 ? `${seconds / 60} min` : `${seconds} s`;
}

$('#start-btn').addEventListener('click', async () => {
  const ok = await modal({
    title: 'Begin the examination?',
    html: `<p>The timer of <strong>${formatDuration(app.info.durationSeconds)}</strong> starts as soon as you begin and cannot be paused.</p>`
      + '<p class="muted">Please stay on this window until you submit.</p>',
    confirm: 'Begin now', cancel: 'Not yet',
  });
  if (!ok) return;
  try {
    beginExam(await api('exam/start', {}));
  } catch (err) {
    toast(err.message, 'bad');
  }
});

$('#last-result-btn').addEventListener('click', showResult);

/* ------------------------------------------------------------------ exam */

function beginExam(exam) {
  app.exam = exam;
  app.finishing = false;
  const firstOpen = exam.questions.findIndex(q => exam.answers[q.id] === undefined);
  app.current = firstOpen < 0 ? 0 : firstOpen;
  $('#exam-title').textContent = app.info ? app.info.title : 'Examination';
  buildPalette();
  renderQuestion();
  updateIntegrity();
  startTimer(exam.remainingSeconds);
  show('exam');
}

function currentQuestion() {
  return app.exam.questions[app.current];
}

function answeredCount() {
  return app.exam.questions.filter(q => app.exam.answers[q.id] !== undefined).length;
}

function renderQuestion() {
  const q = currentQuestion();
  const total = app.exam.questions.length;
  const chosen = app.exam.answers[q.id];
  $('#q-section').textContent = `Section ${q.section} · ${q.sectionTitle}`;
  $('#q-count').textContent = `Question ${app.current + 1} of ${total}`;
  $('#q-text').textContent = q.text;
  $('#q-options').innerHTML = q.options.map((opt, i) => `
    <button type="button" class="option${chosen === i ? ' selected' : ''}" data-index="${i}"
            role="radio" aria-checked="${chosen === i}">
      <span class="opt-letter">${LETTERS[i]}</span><span class="opt-text">${esc(opt)}</span>
    </button>`).join('');
  $('#prev-btn').disabled = app.current === 0;
  $('#next-btn').textContent = app.current === total - 1 ? 'Review & submit' : 'Next →';
  updatePalette();
  updateProgress();
}

function buildPalette() {
  const groups = new Map();
  app.exam.questions.forEach((q, i) => {
    if (!groups.has(q.section)) groups.set(q.section, { title: q.sectionTitle, items: [] });
    groups.get(q.section).items.push(i);
  });
  $('#palette').innerHTML = [...groups].map(([id, g]) => `
    <div class="pal-group">
      <div class="pal-title">Section ${esc(id)} <span>· ${esc(g.title)}</span></div>
      <div class="pal-grid">${g.items.map(i => `<button type="button" class="pal-btn" data-i="${i}">${i + 1}</button>`).join('')}</div>
    </div>`).join('');
}

function updatePalette() {
  $$('.pal-btn').forEach(b => {
    const i = Number(b.dataset.i);
    b.classList.toggle('answered', app.exam.answers[app.exam.questions[i].id] !== undefined);
    b.classList.toggle('current', i === app.current);
  });
}

function updateProgress() {
  const total = app.exam.questions.length;
  const done = answeredCount();
  $('#progress-fill').style.width = (done / total * 100) + '%';
  $('#progress-label').textContent = `${done} of ${total} answered`;
}

function updateIntegrity() {
  const { violations, maxViolations, malpractice } = app.exam;
  $('#viol-count').textContent = `${violations} / ${maxViolations}`;
  const box = $('#integrity');
  box.classList.toggle('warn', violations > 0 && !malpractice);
  box.classList.toggle('bad', malpractice);
}

$('#palette').addEventListener('click', e => {
  const b = e.target.closest('.pal-btn');
  if (!b) return;
  app.current = Number(b.dataset.i);
  renderQuestion();
});

$('#prev-btn').addEventListener('click', () => {
  if (app.current > 0) { app.current--; renderQuestion(); }
});

$('#next-btn').addEventListener('click', () => {
  if (app.current < app.exam.questions.length - 1) { app.current++; renderQuestion(); } else openSubmit();
});

$('#q-options').addEventListener('click', async e => {
  const b = e.target.closest('.option');
  if (!b || app.finishing) return;
  const q = currentQuestion();
  const index = Number(b.dataset.index);
  const previous = app.exam.answers[q.id];
  app.exam.answers[q.id] = index;
  renderQuestion();
  try {
    await api('exam/answer', { questionId: q.id, option: index });
  } catch (err) {
    if (previous === undefined) delete app.exam.answers[q.id]; else app.exam.answers[q.id] = previous;
    renderQuestion();
    if (err.status === 409) await checkEnded(err.message); else toast(err.message, 'bad');
  }
});

document.addEventListener('keydown', e => {
  if (document.body.dataset.view !== 'exam' || !$('#modal').hidden || app.finishing) return;
  if (e.key === 'ArrowRight') $('#next-btn').click();
  else if (e.key === 'ArrowLeft' && app.current > 0) $('#prev-btn').click();
  else {
    const i = LETTERS.indexOf(e.key.toUpperCase());
    const opt = $$('#q-options .option')[i];
    if (i >= 0 && opt) opt.click();
  }
});

$('#submit-btn').addEventListener('click', openSubmit);

async function openSubmit() {
  if (app.finishing) return;
  const total = app.exam.questions.length;
  const done = answeredCount();
  const sections = [...new Set(app.exam.questions.map(q => q.section))];
  const missing = sections.filter(s =>
    !app.exam.questions.some(q => q.section === s && app.exam.answers[q.id] !== undefined));
  const ok = await modal({
    title: 'Submit your examination?',
    html: `<p>You have answered <strong>${done}</strong> of ${total} questions`
      + (total - done ? `; <strong>${total - done}</strong> left unanswered.` : '.') + '</p>'
      + (missing.length ? `<p class="alert alert-warn">Section ${missing.join(', ')} has not been attempted. A section left blank means the result will be FAIL.</p>` : '')
      + '<p class="muted">Once submitted, your answers are final and cannot be changed.</p>',
    confirm: 'Submit now', cancel: 'Keep reviewing',
  });
  if (!ok || app.finishing) return;
  app.finishing = true;
  stopTimer();
  try {
    renderResult(await api('exam/submit', {}));
  } catch (err) {
    app.finishing = false;
    toast(err.message, 'bad');
  }
}

/* timer */

function startTimer(seconds) {
  stopTimer();
  app.deadline = Date.now() + seconds * 1000;
  tick();
  app.timer = setInterval(tick, 250);
}

function stopTimer() {
  clearInterval(app.timer);
  app.timer = null;
}

function tick() {
  const ms = Math.max(0, app.deadline - Date.now());
  const s = Math.ceil(ms / 1000);
  $('#timer-text').textContent = String(Math.floor(s / 60)).padStart(2, '0') + ':' + String(s % 60).padStart(2, '0');
  const t = $('#timer');
  t.classList.toggle('warn', s <= 120 && s > 60);
  t.classList.toggle('danger', s <= 60);
  if (ms <= 0) {
    stopTimer();
    timeUp();
  }
}

async function timeUp() {
  if (app.finishing) return;
  app.finishing = true;
  // The server owns the clock: wait until it reports the exam as closed.
  for (let i = 0; i < 10; i++) {
    try {
      const s = await api('exam/state');
      if (s.state !== 'IN_PROGRESS') break;
    } catch { break; }
    await sleep(500);
  }
  await modal({
    title: 'Time is up',
    html: '<p>The examination time has elapsed. Your answers were locked automatically and the paper has been closed.</p>',
    confirm: 'View result', cancel: null,
  });
  showResult();
}

async function checkEnded(message) {
  try {
    const s = await api('exam/state');
    if (s.state === 'TIMED_OUT') { stopTimer(); app.finishing = false; timeUp(); return; }
    if (s.state === 'SUBMITTED') { showResult(); return; }
  } catch { /* fall through */ }
  toast(message, 'bad');
}

/* integrity: leaving the window is recorded by the server */

document.addEventListener('visibilitychange', async () => {
  if (document.body.dataset.view !== 'exam' || app.finishing) return;
  if (document.hidden) {
    app.pendingViolation = api('exam/violation', {}).catch(() => null);
    return;
  }
  const r = app.pendingViolation && await app.pendingViolation;
  app.pendingViolation = null;
  if (!r) return;
  Object.assign(app.exam, r);
  updateIntegrity();
  if (r.malpractice) {
    toast(`Malpractice recorded: you left the exam window ${r.violations} times. Your result will be withheld.`, 'bad', 6500);
  } else {
    toast(`Warning ${r.violations} of ${r.maxViolations}: leaving the exam window is recorded.`, 'warn', 5500);
  }
});

/* ------------------------------------------------------------------ result */

async function showResult() {
  try {
    renderResult(await api('result'));
  } catch (err) {
    toast(err.message, 'bad');
  }
}

function renderResult(r) {
  stopTimer();
  app.finishing = true;

  $('#r-name').textContent = r.name;
  $('#r-exam').textContent = `${r.examTitle} · `
    + (r.endState === 'TIMED_OUT' ? 'Closed automatically at time-out' : 'Submitted by the candidate');
  $('#r-grade').textContent = r.grade;
  $('#r-pct').textContent = num(r.percentage) + '%';

  const fill = $('#ring-fill');
  const circumference = 2 * Math.PI * 60;
  fill.style.transition = 'none';
  fill.style.strokeDasharray = circumference;
  fill.style.strokeDashoffset = circumference;
  fill.getBoundingClientRect();
  fill.style.transition = '';
  requestAnimationFrame(() => { fill.style.strokeDashoffset = circumference * (1 - r.percentage / 100); });

  const status = $('#r-status');
  status.className = 'status status-' + r.status;
  status.textContent = r.status;

  const reasons = {
    PASS: 'Congratulations, you have met every requirement to pass.',
    WITHHELD: `The result is withheld because malpractice was recorded (the exam window was left ${r.violations} times).`,
    FAIL: r.percentage < r.passPercentage
      ? `The score is below the pass mark of ${num(r.passPercentage)}%.`
      : 'Not every section was attempted, which is required to pass.',
  };
  $('#r-reason').textContent = reasons[r.status];

  $('#r-stats').innerHTML = [
    ['Score', `${num(r.score)} / ${num(r.maxScore)}`],
    ['Correct', r.correct],
    ['Wrong', r.wrong],
    ['Unanswered', r.unanswered],
  ].map(([k, v]) => `<div><dt>${k}</dt><dd>${v}</dd></div>`).join('');

  const checks = [
    { label: 'No malpractice recorded', ok: !r.malpractice, detail: `${r.violations} of ${r.maxViolations} warnings` },
    { label: `Percentage at least ${num(r.passPercentage)}%`, ok: r.percentage >= r.passPercentage, detail: `${num(r.percentage)}%` },
    {
      label: 'Every section attempted', ok: r.allSectionsAttempted,
      detail: r.sections.map(s => `${s.id}: ${s.attempted ? 'yes' : 'no'}`).join(', '),
    },
  ];
  $('#r-checks').innerHTML = checks.map(c => `
    <li><span class="mark ${c.ok ? 'ok' : 'no'}">${c.ok ? '✓' : '✕'}</span>${esc(c.label)}
        <span class="detail">${esc(c.detail)}</span></li>`).join('')
    + `<li class="decision">Decision: ${r.status}`
    + (r.malpractice ? ' (malpractice overrides marks)' : '') + '</li>';

  $('#r-sections').innerHTML = `
    <thead><tr><th>Section</th><th class="num">Correct</th><th class="num">Wrong</th><th class="num">Blank</th><th class="num">Marks</th></tr></thead>
    <tbody>${r.sections.map(s => `
      <tr><td><strong>${esc(s.id)}</strong> · ${esc(s.title)}</td>
          <td class="num">${s.correct}</td><td class="num">${s.wrong}</td>
          <td class="num">${s.unanswered}</td><td class="num">${num(s.marks)}</td></tr>`).join('')}
    </tbody>`;

  $('#r-review').innerHTML = r.review.map(q => {
    const cls = q.marks > 0 ? 'pos' : q.marks < 0 ? 'neg' : 'zero';
    const marks = q.marks > 0 ? `+${num(q.marks)}` : num(q.marks);
    return `<li>
      <div class="rv-head"><span class="rv-q">${esc(q.text)}</span><span class="rv-marks ${cls}">${marks}</span></div>
      <ul class="rv-opts">${q.options.map((o, i) => {
        const c = i === q.correctOption ? 'correct' : i === q.chosenOption ? 'wrong' : '';
        return `<li class="${c}">${LETTERS[i]}. ${esc(o)}</li>`;
      }).join('')}</ul>
      ${q.chosenOption === null ? '<div class="rv-skip">Not answered</div>' : ''}
    </li>`;
  }).join('');

  show('result');
}

$('#print-btn').addEventListener('click', () => window.print());
$('#back-btn').addEventListener('click', () => enterDashboard().catch(err => toast(err.message, 'bad')));

/* ------------------------------------------------------------------ start-up */

(async function init() {
  if (app.token) {
    try {
      const me = await api('me');
      setUser(me.user);
      app.info = await api('exam/info');
      if (me.exam && me.exam.state === 'IN_PROGRESS') {
        beginExam(await api('exam/start', {}));
        toast('Your examination has been resumed.', 'info');
      } else {
        await enterDashboard();
      }
      return;
    } catch {
      app.token = null;
      sessionStorage.removeItem('oes.token');
      setUser(null);
    }
  }
  show('auth');
})();
