function updateServerInfo() {
  fetch('/api/info')
    .then(res => res.json())
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
  fetch('/api/chat')
    .then(res => res.json())
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

window.addEventListener('load', () => {

    updateServerInfo();
    updateChatLog();

    setInterval(updateServerInfo, 10000)
    setInterval(updateChatLog, 1000);
});




