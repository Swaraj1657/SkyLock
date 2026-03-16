/* =====================================================
   SkyLock — Share Modal (Google Drive Style)
   ===================================================== */

(function () {
    'use strict';

    const shareModal = document.getElementById('share-modal');
    let currentShareType = null; // 'file' or 'folder'
    let currentShareId = null;
    let currentShareName = '';
    let sharedPeople = [];
    let generalAccess = 'restricted'; // 'restricted' or 'anyone'

    // ── Open Share Modal ──────────────────────────────
    skylock.openShareModal = function (type, id, name) {
        currentShareType = type;
        currentShareId = id;
        currentShareName = name;

        document.getElementById('share-modal-title').textContent = name;
        document.getElementById('share-email-input').value = '';
        document.getElementById('share-people-list').innerHTML = '';
        document.getElementById('general-access-label').textContent = 'Restricted';
        document.getElementById('general-access-desc').textContent =
            'Only people with access can open with the link';
        generalAccess = 'restricted';
        updateGeneralAccessIcon();

        // Load existing shares from API
        loadShares();

        if (shareModal) {
            shareModal.classList.add('visible');
            document.getElementById('share-email-input').focus();
        }
    };

    // ── Bind Share Modal ──────────────────────────────
    skylock.bindShareModal = function () {
        const doneBtn = document.getElementById('share-done-btn');
        const copyLinkBtn = document.getElementById('share-copy-link-btn');
        const addBtn = document.getElementById('share-add-btn');
        const emailInput = document.getElementById('share-email-input');
        const generalAccessBtn = document.getElementById('general-access-toggle');

        if (doneBtn) doneBtn.addEventListener('click', closeShareModal);

        if (shareModal) {
            shareModal.addEventListener('click', (e) => {
                if (e.target === shareModal) closeShareModal();
            });
        }

        if (addBtn) {
            addBtn.addEventListener('click', addPerson);
        }

        if (emailInput) {
            emailInput.addEventListener('keydown', (e) => {
                if (e.key === 'Enter') addPerson();
            });
        }

        if (copyLinkBtn) {
            copyLinkBtn.addEventListener('click', copyShareLink);
        }

        if (generalAccessBtn) {
            generalAccessBtn.addEventListener('click', toggleGeneralAccess);
        }
    };

    // ── Load Existing Shares ─────────────────────────
    async function loadShares() {
        try {
            const data = await skylock.apiFetch(
                `/api/share/${currentShareType}/${currentShareId}`
            );
            sharedPeople = data.shares || [];
            if (data.generalAccess) generalAccess = data.generalAccess;
            renderPeopleList();
            updateGeneralAccessUI();
        } catch (e) {
            // API not implemented yet — show empty state
            sharedPeople = [];
            renderPeopleList();
        }
    }

    // ── Add Person ───────────────────────────────────
    async function addPerson() {
        const input = document.getElementById('share-email-input');
        const email = input.value.trim();

        if (!email) {
            skylock.showToast('Please enter an email address', 'error');
            return;
        }

        if (!isValidEmail(email)) {
            skylock.showToast('Please enter a valid email address', 'error');
            return;
        }

        // Check if already added
        if (sharedPeople.find((p) => p.email === email)) {
            skylock.showToast('This person already has access', 'error');
            return;
        }

        try {
            await skylock.apiFetch(
                `/api/share/${currentShareType}/${currentShareId}`,
                {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ email: email, role: 'viewer' }),
                }
            );
            await loadShares();
            input.value = '';
            skylock.showToast('Access granted to ' + email, 'success');
        } catch (e) {
            // If API not ready, still show it in UI for demo
            sharedPeople.push({ email: email, role: 'viewer', name: email.split('@')[0] });
            renderPeopleList();
            input.value = '';
        }
    }

    // ── Render People List ───────────────────────────
    function renderPeopleList() {
        const list = document.getElementById('share-people-list');
        list.innerHTML = '';

        if (sharedPeople.length === 0) {
            list.innerHTML = `
                <div class="share-empty-state">
                    <p>No one else has access yet</p>
                </div>
            `;
            return;
        }

        sharedPeople.forEach((person, index) => {
            const item = document.createElement('div');
            item.className = 'share-person-item';
            const initials = getInitials(person.name || person.email);
            const avatarColor = getAvatarColor(person.email);

            item.innerHTML = `
                <div class="share-person-avatar" style="background: ${avatarColor}">
                    ${initials}
                </div>
                <div class="share-person-info">
                    <span class="share-person-name">${skylock.escapeHtml(person.name || person.email.split('@')[0])}</span>
                    <span class="share-person-email">${skylock.escapeHtml(person.email)}</span>
                </div>
                <div class="share-role-wrapper">
                    <select class="share-role-select" data-index="${index}">
                        <option value="viewer" ${person.role === 'viewer' ? 'selected' : ''}>Viewer</option>
                        <option value="editor" ${person.role === 'editor' ? 'selected' : ''}>Editor</option>
                        <option value="remove" class="share-role-remove">Remove</option>
                    </select>
                </div>
            `;

            const select = item.querySelector('.share-role-select');
            select.addEventListener('change', (e) => {
                const newRole = e.target.value;
                if (newRole === 'remove') {
                    removePerson(index);
                } else {
                    updatePersonRole(index, newRole);
                }
            });

            list.appendChild(item);
        });
    }

    // ── Update Person Role ───────────────────────────
    async function updatePersonRole(index, newRole) {
        const person = sharedPeople[index];
        try {
            await skylock.apiFetch(
                `/api/share/${currentShareType}/${currentShareId}/${person.id || index}`,
                {
                    method: 'PUT',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ role: newRole }),
                }
            );
        } catch (e) {
            // API not implemented yet
        }
        sharedPeople[index].role = newRole;
    }

    // ── Remove Person ────────────────────────────────
    async function removePerson(index) {
        const person = sharedPeople[index];
        try {
            await skylock.apiFetch(
                `/api/share/${currentShareType}/${currentShareId}/${person.id || index}`,
                { method: 'DELETE' }
            );
        } catch (e) {
            // API not implemented yet
        }
        sharedPeople.splice(index, 1);
        renderPeopleList();
        skylock.showToast('Access removed', 'success');
    }

    // ── General Access ───────────────────────────────
    async function toggleGeneralAccess() {
        generalAccess = generalAccess === 'restricted' ? 'anyone' : 'restricted';
        updateGeneralAccessUI();

        try {
            await skylock.apiFetch(
                `/api/share/${currentShareType}/${currentShareId}/general-access`,
                {
                    method: 'PUT',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ access: generalAccess }),
                }
            );
        } catch (e) {
            // API not implemented yet
        }
    }

    function updateGeneralAccessUI() {
        const label = document.getElementById('general-access-label');
        const desc = document.getElementById('general-access-desc');

        if (generalAccess === 'anyone') {
            label.textContent = 'Anyone with the link';
            desc.textContent = 'Anyone on the internet with the link can view';
        } else {
            label.textContent = 'Restricted';
            desc.textContent = 'Only people with access can open with the link';
        }
        updateGeneralAccessIcon();
    }

    function updateGeneralAccessIcon() {
        const icon = document.getElementById('general-access-icon');
        if (!icon) return;

        if (generalAccess === 'anyone') {
            icon.innerHTML = `
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <circle cx="12" cy="12" r="10"/>
                    <line x1="2" y1="12" x2="22" y2="12"/>
                    <path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"/>
                </svg>
            `;
        } else {
            icon.innerHTML = `
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
                    <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
                </svg>
            `;
        }
    }

    // ── Copy Link ────────────────────────────────────
    async function copyShareLink() {
        try {
            const data = await skylock.apiFetch(
                `/api/share/${currentShareType}/${currentShareId}/copy-link`,
                { method: 'POST' }
            );
            if (data.link) {
                await navigator.clipboard.writeText(data.link);
                skylock.showToast('Link copied to clipboard!', 'success');
                return;
            }
        } catch (e) {
            // API not ready — copy current URL as fallback
        }

        const fallbackLink = window.location.origin + `/shared/${currentShareType}/${currentShareId}`;
        try {
            await navigator.clipboard.writeText(fallbackLink);
            skylock.showToast('Link copied to clipboard!', 'success');
        } catch (e) {
            skylock.showToast('Failed to copy link', 'error');
        }
    }

    // ── Helpers ──────────────────────────────────────
    function closeShareModal() {
        if (shareModal) shareModal.classList.remove('visible');
    }

    function isValidEmail(email) {
        return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
    }

    function getInitials(name) {
        if (!name) return '?';
        const parts = name.split(/[\s.@]+/);
        if (parts.length >= 2) {
            return (parts[0][0] + parts[1][0]).toUpperCase();
        }
        return name.substring(0, 2).toUpperCase();
    }

    function getAvatarColor(email) {
        const colors = [
            '#4285f4', '#ea4335', '#fbbc04', '#34a853',
            '#ff6d01', '#46bdc6', '#7b1fa2', '#c2185b',
            '#00897b', '#6d4c41'
        ];
        let hash = 0;
        for (let i = 0; i < email.length; i++) {
            hash = email.charCodeAt(i) + ((hash << 5) - hash);
        }
        return colors[Math.abs(hash) % colors.length];
    }

})();
