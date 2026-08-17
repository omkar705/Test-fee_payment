document.addEventListener('DOMContentLoaded', () => {
    // UI Elements
    const loginView = document.getElementById('loginView');
    const dashboardView = document.getElementById('dashboardView');
    const loginForm = document.getElementById('loginForm');
    const loginError = document.getElementById('loginError');
    const usernameInput = document.getElementById('username');
    const passwordInput = document.getElementById('password');
    const logoutBtn = document.getElementById('logoutBtn');

    const payNowBtn = document.getElementById('payNowBtn');
    const payBtnText = document.getElementById('payBtnText');
    const paySpinner = document.getElementById('paySpinner');

    const modalOverlay = document.getElementById('modalOverlay');
    const closeModalBtn = document.getElementById('closeModalBtn');
    const txnIdElem = document.getElementById('txnId');
    const txnDateElem = document.getElementById('txnDate');
    const txnMethodElem = document.getElementById('txnMethod');

    const methodCards = document.querySelectorAll('.method-card');

    // Static Credentials
    const STATIC_USER = 'student';
    const STATIC_PASS = 'password';

    // Handle Login
    loginForm.addEventListener('submit', (e) => {
        e.preventDefault();
        const user = usernameInput.value.trim();
        const pass = passwordInput.value.trim();

        if (user === STATIC_USER && pass === STATIC_PASS) {
            loginError.classList.add('hidden');
            // Transition to Dashboard
            loginView.classList.add('hidden');
            dashboardView.classList.remove('hidden');
        } else {
            loginError.classList.remove('hidden');
        }
    });

    // Handle Logout
    logoutBtn.addEventListener('click', () => {
        dashboardView.classList.add('hidden');
        loginView.classList.remove('hidden');
        passwordInput.value = 'password';
    });

    // Handle Payment Method Selection
    methodCards.forEach(card => {
        card.addEventListener('click', () => {
            methodCards.forEach(c => c.classList.remove('active'));
            card.classList.add('active');
            const radio = card.querySelector('input[type="radio"]');
            if (radio) radio.checked = true;
        });
    });

    // Handle Payment Submission
    payNowBtn.addEventListener('click', () => {
        // Show loading state
        payNowBtn.disabled = true;
        payBtnText.textContent = 'Processing Payment...';
        paySpinner.classList.remove('hidden');

        // Get selected payment method
        const selectedRadio = document.querySelector('input[name="payMethod"]:checked');
        let methodText = 'Credit Card';
        if (selectedRadio) {
            if (selectedRadio.value === 'upi') methodText = 'UPI / Instant Pay';
            if (selectedRadio.value === 'netbanking') methodText = 'Net Banking';
        }

        // Simulate 1.2s instant processing
        setTimeout(() => {
            // Generate receipt details
            const randomTxn = 'TXN-' + Math.floor(10000 + Math.random() * 90000);
            const now = new Date();
            const dateFormatted = now.toLocaleDateString('en-US', {
                month: 'short',
                day: 'numeric',
                year: 'numeric',
                hour: '2-digit',
                minute: '2-digit'
            });

            txnIdElem.textContent = randomTxn;
            txnDateElem.textContent = dateFormatted;
            txnMethodElem.textContent = methodText;

            // Reset button state
            payNowBtn.disabled = false;
            payBtnText.textContent = 'Pay $5,050.00 Now';
            paySpinner.classList.add('hidden');

            // Show completion popup
            modalOverlay.classList.remove('hidden');
        }, 1200);
    });

    // Close Modal and Reset to Login or Dashboard
    closeModalBtn.addEventListener('click', () => {
        modalOverlay.classList.add('hidden');
        // Reset view back to login screen so every login has static pending fees to pay
        dashboardView.classList.add('hidden');
        loginView.classList.remove('hidden');
    });
});
