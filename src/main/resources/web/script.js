fetch('/api/info')
  .then(res => res.json())
  .then(data => {
    document.getElementById('players-count').textContent = data.players;
    document.getElementById('version').textContent = data.version;
    document.getElementById('motd').textContent = data.motd;
    document.getElementById('devmode').textContent = data.devmode ? 'ON' : 'OFF';
    document.getElementById('api').textContent = data.apiVersion || 'Unknown';

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
    document.getElementById('players-count').textContent = 'Unavailable';
    document.getElementById('version').textContent = 'Unavailable';
    document.getElementById('motd').textContent = 'Unavailable';
    document.getElementById('devmode').textContent = 'Unavailable';
    document.getElementById('player-list').innerHTML = '<li class="list-group-item bg-secondary text-white">Failed to load player list</li>';
  });
