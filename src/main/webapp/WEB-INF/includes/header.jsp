<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!-- Header EasyRide -->
<header class="header">
    <div class="header-content">
        <!-- Logo -->
        <div class="logo">
            <a href="${pageContext.request.contextPath}/">
                🚗 EasyRide
            </a>
        </div>
        
        <!-- Navigazione principale -->
        <nav class="main-nav">
            <ul>
                <li><a href="${pageContext.request.contextPath}/catalogo">Catalogo</a></li>
                <c:if test="${not empty sessionScope.utente}">
                    <li><a href="${pageContext.request.contextPath}/carrello">Carrello <span class="cart-counter">0</span></a></li>
                    <li><a href="${pageContext.request.contextPath}/prenotazioni">Le mie prenotazioni</a></li>
                </c:if>
            </ul>
        </nav>
        
        <!-- User Info -->
        <div class="user-info">
            <c:choose>
                <c:when test="${not empty sessionScope.utente}">
                    <!-- Utente loggato -->
                    <div class="user-welcome">
                        Benvenuto, ${sessionScope.utente.nome}
                    </div>
                    
                    <!-- Link admin se è amministratore -->
                    <c:if test="${sessionScope.utente.ruolo == 'admin'}">
                        <a href="${pageContext.request.contextPath}/admin-dashboard" class="header-btn admin-btn">
                            👨‍💼 Dashboard Admin
                        </a>
                    </c:if>
                    
                    <!-- Logout -->
                    <a href="${pageContext.request.contextPath}/login?action=logout" class="header-btn logout-btn">
                        Logout
                    </a>
                </c:when>
                <c:otherwise>
                    <!-- Utente non loggato -->
                    <a href="${pageContext.request.contextPath}/login" class="header-btn login-btn">
                        Login
                    </a>
                    <a href="${pageContext.request.contextPath}/registrazione" class="header-btn register-btn">
                        Registrati
                    </a>
                </c:otherwise>
            </c:choose>
        </div>
        
        <!-- Menu mobile toggle -->
        <div class="mobile-menu-toggle" onclick="toggleMobileMenu()">
            <span></span>
            <span></span>
            <span></span>
        </div>
    </div>
    
    <!-- Menu mobile -->
    <div class="mobile-menu" id="mobileMenu">
        <nav>
            <ul>
                <li><a href="${pageContext.request.contextPath}/catalogo">Catalogo</a></li>
                <c:if test="${not empty sessionScope.utente}">
                    <li><a href="${pageContext.request.contextPath}/carrello">Carrello</a></li>
                    <li><a href="${pageContext.request.contextPath}/prenotazioni">Le mie prenotazioni</a></li>
                    <c:if test="${sessionScope.utente.ruolo == 'admin'}">
                        <li><a href="${pageContext.request.contextPath}/admin-dashboard">Dashboard Admin</a></li>
                    </c:if>
                    <li><a href="${pageContext.request.contextPath}/login?action=logout">Logout</a></li>
                </c:if>
                <c:if test="${empty sessionScope.utente}">
                    <li><a href="${pageContext.request.contextPath}/login">Login</a></li>
                    <li><a href="${pageContext.request.contextPath}/registrazione">Registrati</a></li>
                </c:if>
            </ul>
        </nav>
    </div>
</header>

<script>
// Toggle menu mobile
function toggleMobileMenu() {
    const mobileMenu = document.getElementById('mobileMenu');
    const toggle = document.querySelector('.mobile-menu-toggle');
    
    mobileMenu.classList.toggle('active');
    toggle.classList.toggle('active');
}

// Chiudi menu mobile quando si clicca su un link
document.addEventListener('DOMContentLoaded', function() {
    const mobileLinks = document.querySelectorAll('.mobile-menu a');
    mobileLinks.forEach(link => {
        link.addEventListener('click', () => {
            document.getElementById('mobileMenu').classList.remove('active');
            document.querySelector('.mobile-menu-toggle').classList.remove('active');
        });
    });
    
    // Chiudi menu mobile quando si clicca fuori
    document.addEventListener('click', function(e) {
        const mobileMenu = document.getElementById('mobileMenu');
        const toggle = document.querySelector('.mobile-menu-toggle');
        
        if (!mobileMenu.contains(e.target) && !toggle.contains(e.target)) {
            mobileMenu.classList.remove('active');
            toggle.classList.remove('active');
        }
    });
});
</script>