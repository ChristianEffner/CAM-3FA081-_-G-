document.addEventListener("DOMContentLoaded", () => {
  const showRegisterLink = document.getElementById("showRegisterLink");
  const registerForm = document.getElementById("registerForm");
  const loginForm = document.getElementById("loginForm");
  const registerError = document.getElementById("registerError");
  const registerSuccess = document.getElementById("registerSuccess");

  // 1) Klick "Hier registrieren" zeigt das Register-Form
  showRegisterLink.addEventListener("click", (event) => {
    event.preventDefault();
    registerForm.style.display = "block";
    // Entweder das Login-Form verstecken:
    // loginForm.style.display = "none";
  });

  // 2) Registrieren
  registerForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    const regUsername = document.getElementById("regUsername").value.trim();
    const regPassword = document.getElementById("regPassword").value.trim();

    // REST-Endpoint
    const apiUrl = "http://localhost:8080/users"; // Dein POST /users

    try {
      const response = await fetch(apiUrl, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          username: regUsername,
          password: regPassword
        })
      });

      if (response.ok) {
        registerError.style.display = "none";
        registerSuccess.style.display = "block";
        registerSuccess.innerText = "Registrierung erfolgreich! Du kannst dich jetzt einloggen.";

        // Optional: Automatisch Login-Form anzeigen, Register-Form ausblenden
        // registerForm.style.display = "none";
        // loginForm.style.display = "block";
      } else {
        const text = await response.text();
        registerError.innerText = "Fehler beim Registrieren: " + text;
        registerError.style.display = "block";
      }

    } catch (error) {
      console.error("Registrierung-Fehler:", error);
      registerError.innerText = "Fehler beim Registrieren: " + error.message;
      registerError.style.display = "block";
    }
  });
});
