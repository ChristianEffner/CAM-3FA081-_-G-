document.addEventListener("DOMContentLoaded", () => {
  const apiBaseUrl = "http://localhost:8080"; // ggf. anpassen
  const loginForm = document.getElementById("loginForm");
  const loginError = document.getElementById("loginError");

  loginForm.addEventListener("submit", async function (event) {
    event.preventDefault();
    const username = document.getElementById("username").value.trim();
    const password = document.getElementById("password").value.trim();

    const apiUrl = `${apiBaseUrl}/users/login`;

    try {
      const response = await fetch(apiUrl, {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify({ username, password })
      });

      if (response.ok) {
        const userData = await response.json();
        localStorage.setItem("isLoggedIn", "true");
        // Hier wird die korrekte userId aus dem Login-Response gespeichert
        localStorage.setItem("userId", userData.id);
        window.location.href = "pages/dashboard.html"; // Weiterleitung
      } else if (response.status === 401) {
        loginError.innerText = "Benutzername oder Passwort falsch!";
        loginError.style.display = "block";
      } else {
        loginError.innerText = "Fehler: " + response.statusText;
        loginError.style.display = "block";
      }
    } catch (error) {
      console.error("Login-Fehler:", error);
      loginError.innerText = "Fehler beim Login: " + error.message;
      loginError.style.display = "block";
    }
  });
});
