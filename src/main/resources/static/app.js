document.addEventListener("DOMContentLoaded", () => {
    // UI Elements - Views
    const loginView = document.getElementById("loginView");
    const signupView = document.getElementById("signupView");
    const dashboardView = document.getElementById("dashboardView");

    // Forms & Links
    const loginForm = document.getElementById("loginForm");
    const signupForm = document.getElementById("signupForm");
    const showSignupLink = document.getElementById("showSignupLink");
    const showLoginLink = document.getElementById("showLoginLink");

    // Inputs & Banners
    const loginError = document.getElementById("loginError");
    const signupError = document.getElementById("signupError");
    const signupSuccessBanner = document.getElementById("signupSuccessBanner");

    const usernameInput = document.getElementById("username");
    const passwordInput = document.getElementById("password");

    const signupNameInput = document.getElementById("signupName");
    const signupEmailInput = document.getElementById("signupEmail");
    const signupMobileInput = document.getElementById("signupMobile");
    const signupPasswordInput = document.getElementById("signupPassword");
    const signupConfirmPasswordInput = document.getElementById("signupConfirmPassword");

    const studentNameElem = document.getElementById("studentName");
    const studentRoleElem = document.getElementById("studentRole");
    const avatarElem = document.getElementById("avatar");
    const logoutBtn = document.getElementById("logoutBtn");

    // Payment Elements
    const payNowBtn = document.getElementById("payNowBtn");
    const payBtnText = document.getElementById("payBtnText");
    const paySpinner = document.getElementById("paySpinner");
    const methodCards = document.querySelectorAll(".method-card");

    const PAYMENT_ID = 1;
    const STUDENT_ID = 1;
    const FEE_AMOUNT = 5050;

    // Users Database (Static fallback + dynamic session)
    const usersDatabase = {
        "student": {
            name: "Alex Morgan",
            email: "alex.morgan@university.edu",
            mobile: "+91 98765 43210",
            password: "password",
            id: "STU-2026-8942"
        }
    };

    let currentUser = null;

    // Show Signup View
    if (showSignupLink) {
        showSignupLink.addEventListener("click", (e) => {
            e.preventDefault();
            loginView.classList.add("hidden");
            signupView.classList.remove("hidden");
            if (loginError) loginError.classList.add("hidden");
            if (signupError) signupError.classList.add("hidden");
            if (signupSuccessBanner) signupSuccessBanner.classList.add("hidden");
        });
    }

    // Show Login View
    if (showLoginLink) {
        showLoginLink.addEventListener("click", (e) => {
            e.preventDefault();
            signupView.classList.add("hidden");
            loginView.classList.remove("hidden");
            if (signupError) signupError.classList.add("hidden");
        });
    }

    // Handle Signup Form Submission (POST to /api/users/signup)
    if (signupForm) {
        signupForm.addEventListener("submit", async (e) => {
            e.preventDefault();
            const name = signupNameInput.value.trim();
            const email = signupEmailInput.value.trim().toLowerCase();
            const mobile = signupMobileInput.value.trim();
            const pass = signupPasswordInput.value.trim();
            const confirmPass = signupConfirmPasswordInput.value.trim();

            if (pass !== confirmPass) {
                signupError.textContent = "Passwords do not match! Please check and try again.";
                signupError.classList.remove("hidden");
                return;
            }

            signupError.classList.add("hidden");

            const signupPayload = {
                name: name,
                email: email,
                phone: mobile,
                password: pass
            };

            try {
                const response = await fetch("/api/users/signup", {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify(signupPayload)
                });

                if (response.ok) {
                    const userKey = email.split("@")[0] || name.toLowerCase().replace(/\s+/g, "");
                    const randomId = "STU-2026-" + Math.floor(1000 + Math.random() * 9000);

                    usersDatabase[userKey] = { name, email, mobile, password: pass, id: randomId };
                    usersDatabase[email] = usersDatabase[userKey];

                    signupForm.reset();
                    signupView.classList.add("hidden");
                    loginView.classList.remove("hidden");
                    if (signupSuccessBanner) signupSuccessBanner.classList.remove("hidden");

                    usernameInput.value = email;
                    passwordInput.value = pass;
                } else {
                    const errorMsg = await response.text();
                    signupError.textContent = errorMsg || "Failed to save user in database.";
                    signupError.classList.remove("hidden");
                }
            } catch (err) {
                console.warn("Backend server unavailable, saving locally for fallback demo...", err);
                const userKey = email.split("@")[0] || name.toLowerCase().replace(/\s+/g, "");
                const randomId = "STU-2026-" + Math.floor(1000 + Math.random() * 9000);
                usersDatabase[userKey] = { name, email, mobile, password: pass, id: randomId };
                usersDatabase[email] = usersDatabase[userKey];

                signupForm.reset();
                signupView.classList.add("hidden");
                loginView.classList.remove("hidden");
                if (signupSuccessBanner) signupSuccessBanner.classList.remove("hidden");
                usernameInput.value = email;
                passwordInput.value = pass;
            }
        });
    }

    // Handle Login Form Submission
    if (loginForm) {
        loginForm.addEventListener("submit", (e) => {
            e.preventDefault();
            const enteredUser = usernameInput.value.trim().toLowerCase();
            const enteredPass = passwordInput.value.trim();

            let foundUser = usersDatabase[enteredUser];

            if ((enteredUser === "student" && enteredPass === "password") || (foundUser && foundUser.password === enteredPass)) {
                currentUser = foundUser || usersDatabase["student"];
                if (loginError) loginError.classList.add("hidden");
                if (signupSuccessBanner) signupSuccessBanner.classList.add("hidden");

                if (studentNameElem) studentNameElem.textContent = currentUser.name;
                if (studentRoleElem) studentRoleElem.textContent = `ID: ${currentUser.id} • Computer Science`;
                
                if (avatarElem) {
                    const initials = currentUser.name.split(" ").map(n => n[0]).join("").toUpperCase().slice(0, 2);
                    avatarElem.textContent = initials || "AM";
                }

                loginView.classList.add("hidden");
                dashboardView.classList.remove("hidden");
            } else {
                if (loginError) loginError.classList.remove("hidden");
            }
        });
    }

    // Logout
    if (logoutBtn) {
        logoutBtn.addEventListener("click", () => {
            dashboardView.classList.add("hidden");
            loginView.classList.remove("hidden");
            if (passwordInput) passwordInput.value = "";
            currentUser = null;
        });
    }

    // Method card selection
    methodCards.forEach((card) => {
        card.addEventListener("click", () => {
            methodCards.forEach((item) => item.classList.remove("active"));
            card.classList.add("active");

            const radio = card.querySelector('input[type="radio"]');
            if (radio) radio.checked = true;
        });
    });

    // Pay Now Button - Trigger Razorpay Order & Standard Checkout
    if (payNowBtn) {
        payNowBtn.addEventListener("click", async () => {
            const selected = document.querySelector('input[name="payMethod"]:checked');
            const paymentMethod = selected ? selected.value : "card";

            payNowBtn.disabled = true;
            if (paySpinner) paySpinner.classList.remove("hidden");
            if (payBtnText) payBtnText.textContent = "Connecting to Razorpay...";

            try {
                // 1. Create order with backend
                const orderRes = await fetch("/api/payments/create-order", {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({
                        paymentId: PAYMENT_ID,
                        studentId: STUDENT_ID,
                        amount: FEE_AMOUNT,
                        currency: "INR"
                    })
                });

                const orderData = await orderRes.json();

                if (!orderRes.ok || !orderData.success) {
                    throw new Error(orderData.message || "Failed to initiate payment order with Razorpay.");
                }

                // 2. Razorpay Modal Options
                const options = {
                    key: orderData.keyId,
                    amount: orderData.amountInPaise,
                    currency: orderData.currency || "INR",
                    name: "University Fee Portal",
                    description: "Student Fee Payment - Fall Semester 2026",
                    order_id: orderData.orderId,
                    image: "https://cdn.razorpay.com/static/assets/logo/rzp.png",
                    prefill: {
                        name: currentUser ? currentUser.name : "Alex Morgan",
                        email: currentUser ? currentUser.email : "alex.morgan@university.edu",
                        contact: currentUser ? currentUser.mobile : "9876543210"
                    },
                    notes: {
                        student_id: String(STUDENT_ID),
                        payment_id: String(PAYMENT_ID),
                        purpose: "Tuition & Associated Fees"
                    },
                    theme: { color: "#4361ee" },
                    modal: {
                        ondismiss: function () {
                            payNowBtn.disabled = false;
                            if (paySpinner) paySpinner.classList.add("hidden");
                            if (payBtnText) payBtnText.textContent = "Pay ₹5,050.00 Now";
                        }
                    },
                    handler: async function (response) {
                        if (payBtnText) payBtnText.textContent = "Verifying Payment...";
                        try {
                            const verifyRes = await fetch("/api/payments/verify-payment", {
                                method: "POST",
                                headers: { "Content-Type": "application/json" },
                                body: JSON.stringify({
                                    razorpayOrderId: response.razorpay_order_id,
                                    razorpayPaymentId: response.razorpay_payment_id,
                                    razorpaySignature: response.razorpay_signature,
                                    paymentId: PAYMENT_ID,
                                    studentId: STUDENT_ID,
                                    amount: FEE_AMOUNT,
                                    paymentMethod: paymentMethod
                                })
                            });

                            const verifyData = await verifyRes.json();

                            if (verifyRes.ok && verifyData.success) {
                                sessionStorage.setItem("lastTransactionId", String(verifyData.transactionId));
                                sessionStorage.setItem("lastPaymentId", String(verifyData.razorpayPaymentId || ""));
                                sessionStorage.setItem("lastTransactionReference", verifyData.transactionReference || "");
                                sessionStorage.setItem("paymentMethod", paymentMethod);
                                sessionStorage.removeItem("paymentError");

                                window.location.href = "payment-success.html?transactionId=" + encodeURIComponent(verifyData.transactionId);
                            } else {
                                sessionStorage.setItem("paymentError", verifyData.message || "Payment verification failed.");
                                window.location.href = "payment-failed.html";
                            }
                        } catch (err) {
                            sessionStorage.setItem("paymentError", err.message || "Error verifying payment with server.");
                            window.location.href = "payment-failed.html";
                        }
                    }
                };

                if (typeof Razorpay === "undefined") {
                    throw new Error("Razorpay Checkout SDK is not loaded. Please check network connection.");
                }

                const rzp = new Razorpay(options);
                rzp.on("payment.failed", function (response) {
                    payNowBtn.disabled = false;
                    if (paySpinner) paySpinner.classList.add("hidden");
                    if (payBtnText) payBtnText.textContent = "Pay ₹5,050.00 Now";
                    const errorDesc = (response && response.error && response.error.description) ? response.error.description : "Payment was declined or failed.";
                    sessionStorage.setItem("paymentError", errorDesc);
                    window.location.href = "payment-failed.html";
                });

                rzp.open();

            } catch (error) {
                payNowBtn.disabled = false;
                if (paySpinner) paySpinner.classList.add("hidden");
                if (payBtnText) payBtnText.textContent = "Pay ₹5,050.00 Now";
                sessionStorage.setItem("paymentError", error.message || "Could not launch Razorpay checkout.");
                window.location.href = "payment-failed.html";
            }
        });
    }
});
