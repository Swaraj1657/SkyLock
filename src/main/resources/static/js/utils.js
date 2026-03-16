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
        if (res.status === 401 || res.status === 403) {
            window.location.href = '/loginPage';
            return;
        }
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

// ── File Actions ──────────────────────────────────
skylock.downloadFile = async function (fileOrId) {
    const fileId = typeof fileOrId === 'object' ? fileOrId.id : fileOrId;
    if (!fileId) return;
    
    skylock.showToast('Starting download...', 'success');
    
    const isPublicPage = window.location.href.includes('/public') || window.location.href.includes('anyone');
    let endpoint = isPublicPage ? `/api/public/files/download/${fileId}` : `/api/files/download/${fileId}`;

    try {
        let res = await fetch(endpoint, { method: 'HEAD', credentials: 'same-origin' });
        
        // Fallback to public if unauthorized on regular api
        if (!res.ok && (res.status === 403 || res.status === 401) && !isPublicPage) {
            endpoint = `/api/public/files/download/${fileId}`;
            res = await fetch(endpoint, { method: 'HEAD', credentials: 'same-origin' });
        }

        if (!res.ok) {
            if (res.status === 403 || res.status === 401) {
                skylock.showToast('Unauthorized: You do not have permission to download this file', 'error');
            } else {
                skylock.showToast('Failed to download file', 'error');
            }
            return;
        }
        
        window.location.href = endpoint;
    } catch (e) {
        skylock.showToast('Network error during download', 'error');
    }
};

skylock.previewFile = async function (fileOrId) {
    const fileId = typeof fileOrId === 'object' ? fileOrId.id : fileOrId;
    if (!fileId) return;

    const isPublicPage = window.location.href.includes('/public') || window.location.href.includes('anyone');
    let endpoint = isPublicPage ? `/api/public/files/preview/${fileId}` : `/api/files/preview/${fileId}`;

    try {
        let res = await fetch(endpoint, { method: 'HEAD', credentials: 'same-origin' });
        
        // Fallback to public if unauthorized
        if (!res.ok && (res.status === 403 || res.status === 401) && !isPublicPage) {
            endpoint = `/api/public/files/preview/${fileId}`;
            res = await fetch(endpoint, { method: 'HEAD', credentials: 'same-origin' });
        }

        if (!res.ok) {
            if (res.status === 403 || res.status === 401) {
                skylock.showToast('Unauthorized: You do not have permission to preview this file', 'error');
            } else {
                skylock.showToast('Failed to preview file', 'error');
            }
            return;
        }
        window.open(endpoint, '_blank');
    } catch (e) {
        skylock.showToast('Network error', 'error');
    }
};
