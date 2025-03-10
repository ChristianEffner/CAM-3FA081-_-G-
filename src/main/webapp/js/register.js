document.addEventListener("DOMContentLoaded", () => {
  const showRegisterLink = document.getElementById("showRegisterLink");
  const registerForm = document.getElementById("registerForm");
  const loginForm = document.getElementById("loginForm");
  const registerError = document.getElementById("registerError");
  const registerSuccess = document.getElementById("registerSuccess");

  // Beim Klick auf "Hier registrieren" wird das Registrierungsformular eingeblendet
  showRegisterLink.addEventListener("click", (event) => {
    event.preventDefault();
    registerForm.style.display = "block";
  });

  // Registrierung absenden
  registerForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    const regUsername = document.getElementById("regUsername").value.trim();
    const regPassword = document.getElementById("regPassword").value.trim();

    // REST-Endpoint POST /users
    const apiUrl = "http://localhost:8080/users";

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
        // JSON-Response parsen – hier ist der User mit der generierten ID enthalten
        const newUser = await response.json();
        // User-ID im localStorage speichern
        localStorage.setItem("userId", newUser.id);
        registerError.style.display = "none";
        registerSuccess.style.display = "block";
        registerSuccess.innerText = "Registrierung erfolgreich! Du kannst dich jetzt einloggen.";
        // Optional: Hier kannst du auch direkt zum Login wechseln oder automatisch einloggen
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
