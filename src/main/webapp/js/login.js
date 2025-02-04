document.addEventListener("DOMContentLoaded", () => {
  const apiBaseUrl = "http://localhost:8080"; // Passe ggf. an, falls dein Server eine andere URL hat
  const loginForm = document.getElementById("loginForm");
  const loginError = document.getElementById("loginError");

  loginForm.addEventListener("submit", async function (event) {
    event.preventDefault();
    const username = document.getElementById("username").value.trim();
    const password = document.getElementById("password").value.trim();

    // Wir posten hier an /users/login (neuer Endpoint in CrudUser).
    const apiUrl = `${apiBaseUrl}/users/login`;

    try {
      const response = await fetch(apiUrl, {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify({ username, password }) // { "username":"...", "password":"..." }
      });

      if (response.ok) {
        // Login erfolgreich
        localStorage.setItem("isLoggedIn", "true");

        // Falls du den User zurückbekommst, könntest du ihn so auslesen:
        // const userData = await response.json();
        // localStorage.setItem("currentUserId", userData.id); // nur als Beispiel

        window.location.href = "pages/dashboard.html"; // Weiterleitung
      } else if (response.status === 401) {
        // Falscher User/PW
        loginError.innerText = "Benutzername oder Passwort falsch!";
        loginError.style.display = "block";
      } else {
        // Irgendein anderer Fehler
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
