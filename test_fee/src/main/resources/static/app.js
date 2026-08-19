document.addEventListener('DOMContentLoaded', () => {
    // UI Elements - Views
    const loginView = document.getElementById('loginView');
    const signupView = document.getElementById('signupView');
    const dashboardView = document.getElementById('dashboardView');

    // UI Elements - Forms & Links
    const loginForm = document.getElementById('loginForm');
    const signupForm = document.getElementById('signupForm');
    const showSignupLink = document.getElementById('showSignupLink');
    const showLoginLink = document.getElementById('showLoginLink');

    // UI Elements - Inputs & Banners
    const loginError = document.getElementById('loginError');
    const signupError = document.getElementById('signupError');
    const signupSuccessBanner = document.getElementById('signupSuccessBanner');

    const usernameInput = document.getElementById('username');
    const passwordInput = document.getElementById('password');

    const signupNameInput = document.getElementById('signupName');
    const signupEmailInput = document.getElementById('signupEmail');
    const signupMobileInput = document.getElementById('signupMobile');
    const signupPasswordInput = document.getElementById('signupPassword');
    const signupConfirmPasswordInput = document.getElementById('signupConfirmPassword');

    const studentNameElem = document.getElementById('studentName');
    const studentRoleElem = document.getElementById('studentRole');
    const avatarElem = document.getElementById('avatar');
    const logoutBtn = document.getElementById('logoutBtn');

    // Payment Elements
    const payNowBtn = document.getElementById('payNowBtn');
    const payBtnText = document.getElementById('payBtnText');
    const paySpinner = document.getElementById('paySpinner');

    // Modal Elements
    const modalOverlay = document.getElementById('modalOverlay');
    const closeModalBtn = document.getElementById('closeModalBtn');
    const txnIdElem = document.getElementById('txnId');
    const txnDateElem = document.getElementById('txnDate');
    const txnMethodElem = document.getElementById('txnMethod');

    const methodCards = document.querySelectorAll('.method-card');

    // User Directory (Fallback static user + dynamically registered user state)
    const usersDatabase = {
        'student': {
            name: 'Alex Morgan',
            email: 'alex.morgan@university.edu',
            mobile: '+91 98765 43210',
            password: 'password',
            id: 'STU-2026-8942'
        }
    };

    // Current Logged In User
    let currentUser = null;

    // View Switching: Show Signup
    showSignupLink.addEventListener('click', (e) => {
        e.preventDefault();
        loginView.classList.add('hidden');
        signupView.classList.remove('hidden');
        loginError.classList.add('hidden');
        signupError.classList.add('hidden');
        signupSuccessBanner.classList.add('hidden');
    });

    // View Switching: Show Login
    showLoginLink.addEventListener('click', (e) => {
        e.preventDefault();
        signupView.classList.add('hidden');
        loginView.classList.remove('hidden');
        signupError.classList.add('hidden');
    });

    // Handle Signup Form Submission (Connecting Frontend to Backend API & Supabase DB)
    signupForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const name = signupNameInput.value.trim();
        const email = signupEmailInput.value.trim().toLowerCase();
        const mobile = signupMobileInput.value.trim();
        const pass = signupPasswordInput.value.trim();
        const confirmPass = signupConfirmPasswordInput.value.trim();

        // Password matching validation
        if (pass !== confirmPass) {
            signupError.textContent = 'Passwords do not match! Please check and try again.';
            signupError.classList.remove('hidden');
            return;
        }

        signupError.classList.add('hidden');

        // Construct JSON Payload matching UserInput model (name, email, phone, password)
        const signupPayload = {
            name: name,
            email: email,
            phone: mobile,
            password: pass
        };

        try {
            // Send HTTP POST to Spring Boot Backend REST Controller (/api/users/signup)
            const response = await fetch('/api/users/signup', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(signupPayload)
            });

            if (response.ok) {
                // Also store user locally for instant static login session
                const userKey = email.split('@')[0] || name.toLowerCase().replace(/\s+/g, '');
                const randomId = 'STU-2026-' + Math.floor(1000 + Math.random() * 9000);

                usersDatabase[userKey] = {
                    name: name,
                    email: email,
                    mobile: mobile,
                    password: pass,
                    id: randomId
                };
                usersDatabase[email] = usersDatabase[userKey];

                // Reset Signup form
                signupForm.reset();

                // Redirect to Login View with success banner
                signupView.classList.add('hidden');
                loginView.classList.remove('hidden');
                signupSuccessBanner.classList.remove('hidden');

                // Pre-fill user email & password
                usernameInput.value = email;
                passwordInput.value = pass;
            } else {
                const errorMsg = await response.text();
                signupError.textContent = errorMsg || 'Failed to save user in database.';
                signupError.classList.remove('hidden');
            }
        } catch (err) {
            console.warn('Backend server unavailable. Saving locally for UI demo...', err);
            // Fallback for independent local static mode
            const userKey = email.split('@')[0] || name.toLowerCase().replace(/\s+/g, '');
            const randomId = 'STU-2026-' + Math.floor(1000 + Math.random() * 9000);

            usersDatabase[userKey] = {
                name: name,
                email: email,
                mobile: mobile,
                password: pass,
                id: randomId
            };
            usersDatabase[email] = usersDatabase[userKey];

            signupForm.reset();
            signupView.classList.add('hidden');
            loginView.classList.remove('hidden');
            signupSuccessBanner.classList.remove('hidden');
            usernameInput.value = email;
            passwordInput.value = pass;
        }
    });

    // Handle Login Submission
    loginForm.addEventListener('submit', (e) => {
        e.preventDefault();
        const enteredUser = usernameInput.value.trim().toLowerCase();
        const enteredPass = passwordInput.value.trim();

        let foundUser = usersDatabase[enteredUser];

        if (foundUser && foundUser.password === enteredPass) {
            currentUser = foundUser;
            loginError.classList.add('hidden');
            signupSuccessBanner.classList.add('hidden');

            studentNameElem.textContent = currentUser.name;
            studentRoleElem.textContent = `ID: ${currentUser.id} • Computer Science`;
            
            const initials = currentUser.name
                .split(' ')
                .map(n => n[0])
                .join('')
                .toUpperCase()
                .slice(0, 2);
            avatarElem.textContent = initials || 'ST';

            loginView.classList.add('hidden');
            dashboardView.classList.remove('hidden');
        } else {
            loginError.classList.remove('hidden');
            signupSuccessBanner.classList.add('hidden');
        }
    });

    // Handle Logout
    logoutBtn.addEventListener('click', () => {
        dashboardView.classList.add('hidden');
        loginView.classList.remove('hidden');
        currentUser = null;
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
        payNowBtn.disabled = true;
        payBtnText.textContent = 'Processing Payment...';
        paySpinner.classList.remove('hidden');

        const selectedRadio = document.querySelector('input[name="payMethod"]:checked');
        let methodText = 'Credit Card';
        if (selectedRadio) {
            if (selectedRadio.value === 'upi') methodText = 'UPI / GooglePay / PhonePe';
            if (selectedRadio.value === 'netbanking') methodText = 'Net Banking';
        }

        setTimeout(() => {
            const randomTxn = 'TXN-' + Math.floor(10000 + Math.random() * 90000);
            const now = new Date();
            const dateFormatted = now.toLocaleDateString('en-IN', {
                month: 'short',
                day: 'numeric',
                year: 'numeric',
                hour: '2-digit',
                minute: '2-digit'
            });

            txnIdElem.textContent = randomTxn;
            txnDateElem.textContent = dateFormatted;
            txnMethodElem.textContent = methodText;

            payNowBtn.disabled = false;
            payBtnText.textContent = 'Pay ₹50,500.00 Now';
            paySpinner.classList.add('hidden');

            modalOverlay.classList.remove('hidden');
        }, 1200);
    });

    // Close Modal and Reset to Login
    closeModalBtn.addEventListener('click', () => {
        modalOverlay.classList.add('hidden');
        dashboardView.classList.add('hidden');
        loginView.classList.remove('hidden');
    });
});
