document.addEventListener("DOMContentLoaded", function () {
    // Sidebar-Toggle für mobile Ansicht
    const toggleButton = document.createElement("button");
    toggleButton.classList.add("toggle-sidebar");
    toggleButton.innerHTML = "☰";
    document.body.appendChild(toggleButton);

    const sidebar = document.getElementById("sidebar");
    toggleButton.addEventListener("click", function () {
        sidebar.classList.toggle("collapsed");
    });

    // Logout-Logik
    const logoutButton = document.querySelector(".nav-link[href='logout.html']");
    if (logoutButton) {
        logoutButton.addEventListener("click", function () {
            localStorage.removeItem("isLoggedIn");
            localStorage.removeItem("userId");
            window.location.href = "../index.html";
        });
    }

    // Check auf Login-Status
    if (localStorage.getItem("isLoggedIn") !== "true" && !window.location.href.includes("index.html")) {
        window.location.href = "../index.html";
    }

    // Dynamischer Zähler für Statistiken (z. B. Kunden, Ablesungen)
    if (document.getElementById("customerCount")) {
        fetch("http://localhost:8080/customers")
            .then((response) => response.json())
            .then((data) => {
                document.getElementById("customerCount").textContent = data.length;
            })
            .catch((error) => console.error("Fehler beim Abrufen der Kundenanzahl:", error));
    }

    if (document.getElementById("readingCount")) {
        fetch("http://localhost:8080/readings")
            .then((response) => response.json())
            .then((data) => {
                document.getElementById("readingCount").textContent = data.length;
            })
            .catch((error) => console.error("Fehler beim Abrufen der Ablesungen:", error));
    }

    // Globales Handling für modale Probleme (Fix für Particles und Modale)
    function fixModalOverlay() {
        const modalBackdrops = document.querySelectorAll(".modal-backdrop");
        modalBackdrops.forEach((backdrop) => backdrop.remove());
    }

    document.querySelectorAll(".modal").forEach((modal) => {
        modal.addEventListener("show.bs.modal", fixModalOverlay);
        modal.addEventListener("hidden.bs.modal", fixModalOverlay);
    });
});
