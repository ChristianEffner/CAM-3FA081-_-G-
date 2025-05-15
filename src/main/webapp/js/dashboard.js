document.addEventListener("DOMContentLoaded", () => {
  const apiBaseUrl = "http://localhost:8080";
  const userId = localStorage.getItem("userId");

  // Kein Login? → zurück zur Startseite
  if (!userId) {
    window.location.href = "index.html";
    return;
  }

  // ---------------- Kunden zählen ----------------
  fetch(`${apiBaseUrl}/customers?userId=${userId}`)
    .then(res => res.json())
    .then(customers => {
      document.getElementById("customerCount").textContent = customers.length;
    })
    .catch(err => {
      console.error("Error fetching customers:", err);
      document.getElementById("customerCount").textContent = "0";
    });

  // --------------- Readings zählen ---------------
  fetch(`${apiBaseUrl}/readings?userId=${userId}`)
    .then(res => res.json())
    .then(readings => {
      document.getElementById("readingCount").textContent = readings.length;
    })
    .catch(err => {
      console.error("Error fetching readings:", err);
      document.getElementById("readingCount").textContent = "0";
    });
});
