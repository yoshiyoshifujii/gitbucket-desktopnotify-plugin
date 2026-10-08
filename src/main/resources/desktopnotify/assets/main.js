(function () {
  var path = document.currentScript.dataset.path;
  var CURSOR_KEY = "gitbucket-desktopnotify-cursor";
  var POLL_INTERVAL = 30000;
  var timer = null;

  function storageGet(key) {
    try { return window.localStorage.getItem(key); } catch (e) { return null; }
  }

  function storageSet(key, value) {
    try { window.localStorage.setItem(key, value); } catch (e) { /* private mode etc.: no cursor */ }
  }

  function show(activity) {
    var body = activity.message;
    if (activity.additionalInfo) body += "\n" + activity.additionalInfo;
    var notification = new Notification(activity.repository, {
      body: body,
      icon: path + "/assets/common/images/gitbucket.png",
      // Same tag in every open tab, so the browser shows each activity only once
      tag: "gitbucket-activity-" + activity.id
    });
    notification.onclick = function () {
      window.focus();
      window.location.href = activity.url;
      notification.close();
    };
  }

  function poll() {
    var since = storageGet(CURSOR_KEY);
    var url = path + "/dnotify/activities" + (since ? "?since=" + encodeURIComponent(since) : "");
    fetch(url, { credentials: "same-origin", headers: { Accept: "application/json" } })
      .then(function (response) {
        if (response.status === 401) { stop(); return null; } // signed out
        return response.ok ? response.json() : null;
      })
      .then(function (data) {
        if (!data || !data.latest) return;
        // Another tab may have moved the cursor in the meantime
        if (storageGet(CURSOR_KEY) !== since) return;
        storageSet(CURSOR_KEY, data.latest);
        data.activities.slice().reverse().forEach(show);
      })
      .catch(function () { /* offline or server restarting: try again next time */ });
  }

  function run() {
    poll();
    timer = setInterval(poll, POLL_INTERVAL);
  }

  function stop() {
    clearInterval(timer);
  }

  if (!("Notification" in window) || !window.fetch) return;

  // Browsers only show the permission prompt for a user gesture, and close it again when the page navigates away.
  // So ask from a header button that doesn't navigate, never from an arbitrary click (which is often on a link).
  function showEnableButton() {
    var menu = document.querySelector(".navbar-custom-menu > ul.nav");
    if (!menu) return;
    var item = document.createElement("li");
    item.className = "desktopnotify-enable";
    var button = document.createElement("a");
    button.href = "#";
    button.setAttribute("role", "button");
    button.title = "Enable desktop notifications";
    button.setAttribute("aria-label", button.title);
    button.innerHTML = '<i class="octicon octicon-bell" style="color: black;"></i>';
    button.addEventListener("click", function (event) {
      event.preventDefault();
      Notification.requestPermission().then(function (permission) {
        if (permission === "default") return; // prompt dismissed: keep the button
        item.remove();
        if (permission === "granted") run();
      });
    });
    item.appendChild(button);
    menu.insertBefore(item, menu.firstChild);
  }

  if (Notification.permission === "granted") {
    run();
  } else if (Notification.permission === "default") {
    // Only signed-in users get notifications; the endpoint answers 401 otherwise
    fetch(path + "/dnotify/activities", { credentials: "same-origin" }).then(function (response) {
      if (response.ok) {
        if (document.readyState === "loading") document.addEventListener("DOMContentLoaded", showEnableButton);
        else showEnableButton();
      }
    }).catch(function () {});
  }
})();
