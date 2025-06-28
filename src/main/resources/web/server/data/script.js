async function showPlayerDetails(uuid) {
    const res = await fetch(`/api/player/details?uuid=${uuid}`);
    if (!res.ok) {
        alert("Player not found");
        return;
    }
    const data = await res.json();

    document.getElementById('modalUsername').innerText = data.username;
    document.getElementById('modalEmail').value = data.email || 'N/A';
    document.getElementById('modalPerm').innerText = data.perm;

    const permList = document.getElementById('modalPerms');
    permList.innerHTML = '';
    data.permissions.forEach(perm => {
        const li = document.createElement('li');
        li.classList.add('list-group-item', 'd-flex', 'justify-content-between', 'align-items-center');
        li.innerHTML = `
            ${perm}
            <button class="btn btn-sm btn-danger" onclick="removePerm('${uuid}', '${perm}')">&times;</button>
        `;
        permList.appendChild(li);
    });

    const modal = new bootstrap.Modal(document.getElementById('playerModal'));
    modal.show();
}
