/* =====================================================
   SkyLock — Shared Utilities
   ===================================================== */

window.skylock = window.skylock || {};

// ── Toast ────────────────────────────────────────
skylock.showToast = function (msg, type) {
    const toastEl = document.getElementById('toast');
    if (!toastEl) return;
    toastEl.textContent = msg;
    toastEl.className = 'toast ' + type + ' visible';
    setTimeout(() => toastEl.classList.remove('visible'), 3000);
};

// ── API Fetch ────────────────────────────────────
skylock.apiFetch = async function (url, options = {}) {
    try {
        const res = await fetch(url, options);
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        return await res.json();
    } catch (e) {
        skylock.showToast(e.message || 'Network error', 'error');
        throw e;
    }
};

// ── Format Helpers ───────────────────────────────
skylock.formatSize = function (bytes) {
    if (!bytes || bytes === 0) return '0 B';
    const k = 1024;
    const sizes = ['B', 'KB', 'MB', 'GB', 'TB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i];
};

skylock.formatDate = function (dateStr) {
    if (!dateStr) return 'Unknown';
    try {
        const d = new Date(dateStr);
        const now = new Date();
        const diff = now - d;
        const mins = Math.floor(diff / 60000);
        if (mins < 1) return 'Just now';
        if (mins < 60) return mins + 'm ago';
        const hrs = Math.floor(mins / 60);
        if (hrs < 24) return hrs + 'h ago';
        const days = Math.floor(hrs / 24);
        if (days < 7) return days + 'd ago';
        return d.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
    } catch {
        return dateStr;
    }
};

skylock.escapeHtml = function (str) {
    if (!str) return '';
    const div = document.createElement('div');
    div.textContent = str;
    return div.innerHTML;
};
