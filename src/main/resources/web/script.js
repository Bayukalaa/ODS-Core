function isLoggedIn() {
  return !!sessionStorage.getItem('odsToken');
}

function getAuthHeaders() {
  const token = sessionStorage.getItem('odsToken');
  return token ? { 'Authorization': 'Bearer ' + token } : {};
}

function updateNavbar() {
  const loginBtn = document.getElementById('loginBtn');
  const settingsBtn = document.getElementById('settingsBtn');
  const logoutBtn = document.getElementById('logoutBtn');

  if (isLoggedIn()) {
    loginBtn.classList.add('d-none');
    settingsBtn.classList.remove('d-none');
    logoutBtn.classList.remove('d-none');
  } else {
    loginBtn.classList.remove('d-none');
    settingsBtn.classList.add('d-none');
    logoutBtn.classList.add('d-none');
  }
}

function updateServerInfo() {
  fetch('/api/info', { headers: getAuthHeaders() })
    .then(res => {
      if (res.status === 401 || res.status === 403) throw new Error('Unauthorized');
      return res.json();
    })
    .then(data => {
      document.getElementById('players-count').textContent = data.players;
      document.getElementById('version').textContent = data.version;
      document.getElementById('motd').textContent = data.motd;
      document.getElementById('devmode').textContent = data.devmode ? 'ON' : 'OFF';
      document.getElementById('api-version').textContent = data.apiVersion || 'Unknown';

      document.getElementById('server-ping').textContent = data.ping || 'N/A';
      document.getElementById('server-tps').textContent = data.tps || 'N/A';
      document.getElementById('cpu-usage').textContent = data.cpuUsage ? data.cpuUsage.toFixed(2) : 'N/A';
      document.getElementById('memory-usage').textContent = data.memoryUsage || 'N/A';

      const playerList = document.getElementById('player-list');
      playerList.innerHTML = '';
      if (data.playerNames && data.playerNames.length > 0) {
        data.playerNames.forEach(player => {
          const li = document.createElement('li');
          li.className = 'list-group-item bg-secondary text-white';
          li.textContent = player;
          playerList.appendChild(li);
        });
      } else {
        const li = document.createElement('li');
        li.className = 'list-group-item bg-secondary text-white';
        li.textContent = 'No players online';
        playerList.appendChild(li);
      }
    })
    .catch(err => {
      console.error('Failed to fetch server info:', err);
    });
}

function updateChatLog() {
  fetch('/api/chat', { headers: getAuthHeaders() })
    .then(res => {
      if (res.status === 401 || res.status === 403) throw new Error('Unauthorized');
      return res.json();
    })
    .then(messages => {
      const chatLog = document.getElementById('chat-log');
      chatLog.textContent = '';
      if (messages.length > 0) {
        messages.forEach(msg => {
          chatLog.textContent += msg + '\n';
        });
      } else {
        chatLog.textContent = 'No chat messages.';
      }
      chatLog.scrollTop = chatLog.scrollHeight;
    })
    .catch(err => {
      console.error('Failed to fetch chat log:', err);
    });
}

document.getElementById('loginForm').addEventListener('submit', function (e) {
  e.preventDefault();

  const username = document.getElementById('username').value.trim();
  const password = document.getElementById('password').value;

  fetch('/api/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, password })
  })
    .then(res => res.json())
    .then(data => {
      if (data.status === 'success') {
        sessionStorage.setItem('odsToken', data.token);
        sessionStorage.setItem('odsUsername', username);
        updateNavbar();
        const loginModal = bootstrap.Modal.getInstance(document.getElementById('loginModal'));
        loginModal.hide();
        document.getElementById('loginError').classList.add('d-none');
      } else {
        document.getElementById('loginError').classList.remove('d-none');
      }
    })
    .catch(err => {
      console.error('Login failed:', err);
      document.getElementById('loginError').classList.remove('d-none');
    });
});

document.getElementById('logoutBtn').addEventListener('click', () => {
  const token = sessionStorage.getItem('odsToken');
  fetch('/api/logout', {
    method: 'POST',
    headers: { 'Authorization': 'Bearer ' + token }
  }).finally(() => {
    sessionStorage.removeItem('odsToken');
    sessionStorage.removeItem('odsUsername');
    updateNavbar();
    alert('Logged out.');
  });
});

window.addEventListener('load', () => {
  updateNavbar();
  updateServerInfo();
  updateChatLog();
  setInterval(updateServerInfo, 10000);
  setInterval(updateChatLog, 1000);
});