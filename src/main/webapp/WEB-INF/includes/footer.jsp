<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!-- Footer EasyRide -->
<footer class="footer">
    <div class="footer-content">
        <!-- Colonna 1: About -->
        <div class="footer-section">
            <h4>🚗 EasyRide</h4>
            <p>
                Il tuo partner di fiducia per il noleggio veicoli. 
                Offriamo una vasta gamma di veicoli per ogni esigenza, 
                con il miglior servizio clienti del settore.
            </p>
            <div class="social-links">
                <a href="#" aria-label="Facebook">📘</a>
                <a href="#" aria-label="Instagram">📷</a>
                <a href="#" aria-label="Twitter">🐦</a>
                <a href="#" aria-label="LinkedIn">💼</a>
            </div>
        </div>
        
        <!-- Colonna 2: Link Utili -->
        <div class="footer-section">
            <h4>Link Utili</h4>
            <ul>
                <li><a href="${pageContext.request.contextPath}/catalogo">Catalogo Veicoli</a></li>
                <li><a href="${pageContext.request.contextPath}/come-funziona">Come Funziona</a></li>
                <li><a href="${pageContext.request.contextPath}/tariffe">Tariffe</a></li>
                <li><a href="${pageContext.request.contextPath}/faq">FAQ</a></li>
                <li><a href="${pageContext.request.contextPath}/assistenza">Assistenza</a></li>
            </ul>
        </div>
        
        <!-- Colonna 3: Account -->
        <div class="footer-section">
            <h4>Il Tuo Account</h4>
            <ul>
                <c:choose>
                    <c:when test="${not empty sessionScope.utente}">
                        <li><a href="${pageContext.request.contextPath}/profilo">Il Mio Profilo</a></li>
                        <li><a href="${pageContext.request.contextPath}/prenotazioni">Le Mie Prenotazioni</a></li>
                        <li><a href="${pageContext.request.contextPath}/carrello">Carrello</a></li>
                        <c:if test="${sessionScope.utente.ruolo == 'admin'}">
                            <li><a href="${pageContext.request.contextPath}/admin-dashboard">Dashboard Admin</a></li>
                        </c:if>
                        <li><a href="${pageContext.request.contextPath}/login?action=logout">Logout</a></li>
                    </c:when>
                    <c:otherwise>
                        <li><a href="${pageContext.request.contextPath}/login">Accedi</a></li>
                        <li><a href="${pageContext.request.contextPath}/registrazione">Registrati</a></li>
                        <li><a href="${pageContext.request.contextPath}/recupera-password">Password Dimenticata</a></li>
                    </c:otherwise>
                </c:choose>
            </ul>
        </div>
        
        <!-- Colonna 4: Contatti -->
        <div class="footer-section">
            <h4>Contattaci</h4>
            <div class="contact-info">
                <p>
                    <strong>📍 Sede Principale</strong><br>
                    Via Roma 123, 00100 Roma (RM)
                </p>
                <p>
                    <strong>📞 Telefono</strong><br>
                    <a href="tel:+390612345678">+39 06 1234 5678</a>
                </p>
                <p>
                    <strong>✉️ Email</strong><br>
                    <a href="mailto:info@easyride.it">info@easyride.it</a>
                </p>
                <p>
                    <strong>🕒 Orari</strong><br>
                    Lun-Ven: 8:00-20:00<br>
                    Sab-Dom: 9:00-18:00
                </p>
            </div>
        </div>
    </div>
    
    <!-- Bottom Bar -->
    <div class="footer-bottom">
        <div class="footer-bottom-content">
            <div class="copyright">
                <p>&copy; 2024 EasyRide. Tutti i diritti riservati.</p>
            </div>
            <div class="legal-links">
                <a href="${pageContext.request.contextPath}/privacy">Privacy Policy</a>
                <a href="${pageContext.request.contextPath}/termini">Termini di Servizio</a>
                <a href="${pageContext.request.contextPath}/cookies">Cookie Policy</a>
            </div>
        </div>
    </div>
</footer>