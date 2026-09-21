document.addEventListener("DOMContentLoaded", () => {
    const loginView = document.getElementById("loginView");
    const signupView = document.getElementById("signupView");
    const dashboardView = document.getElementById("dashboardView");

    const loginForm = document.getElementById("loginForm");
    const signupForm = document.getElementById("signupForm");
    const showSignupLink = document.getElementById("showSignupLink");
    const showLoginLink = document.getElementById("showLoginLink");

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

    const payNowBtn = document.getElementById("payNowBtn");
    const payBtnText = document.getElementById("payBtnText");
    const paySpinner = document.getElementById("paySpinner");
    const methodCards = document.querySelectorAll(".method-card");
    const paymentHistoryList = document.getElementById("paymentHistoryList");
    const latestReceiptCard = document.getElementById("latestReceiptCard");

    const PAYMENT_ID = 1;
    const STUDENT_ID = 1;
    const FEE_AMOUNT = 5050;

    const STORAGE_KEYS = {
        paymentHistory: "studentPaymentHistory",
        paymentMethod: "paymentMethod",
        lastTransactionId: "lastTransactionId",
        lastPaymentId: "lastPaymentId",
        lastTransactionReference: "lastTransactionReference",
        paymentError: "paymentError"
    };

    const usersDatabase = {
        student: {
            name: "Alex Morgan",
            email: "alex.morgan@university.edu",
            mobile: "+91 98765 43210",
            password: "password",
            id: "STU-2026-8942"
        }
    };

    let currentUser = null;

    function formatCurrency(amount) {
        return new Intl.NumberFormat("en-IN", {
            style: "currency",
            currency: "INR",
            minimumFractionDigits: 2
        }).format(Number(amount || 0));
    }

    function clearSessionPaymentData() {
        Object.values(STORAGE_KEYS).forEach((key) => {
            sessionStorage.removeItem(key);
        });
    }

    function readPaymentHistory() {
        try {
            const raw = sessionStorage.getItem(STORAGE_KEYS.paymentHistory);
            return raw ? JSON.parse(raw) : [];
        } catch (error) {
            return [];
        }
    }

    function writePaymentHistory(items) {
        sessionStorage.setItem(STORAGE_KEYS.paymentHistory, JSON.stringify(items));
        renderPaymentHistory(items);
        renderLatestReceipt(items[0] || null);
    }

    function renderPaymentHistory(items = readPaymentHistory()) {
        if (!paymentHistoryList) return;

        if (!items || !items.length) {
            paymentHistoryList.innerHTML = '<div class="empty-state">No payment history yet. Your recent fee payments will appear here.</div>';
            return;
        }

        paymentHistoryList.innerHTML = items.slice(0, 5).map((item) => {
            const transactionReference = item.transactionReference || item.reference || "Payment";
            const amount = item.amount || item.total || FEE_AMOUNT;
            const receiptUrl = item.receiptUrl || `receipt.html?transactionId=${item.transactionId || item.id || ""}`;
            const date = item.transactionDate ? new Date(item.transactionDate).toLocaleString("en-IN", {
                dateStyle: "medium",
                timeStyle: "short"
            }) : "—";
            const status = (item.transactionStatus || item.status || "SUCCESS").toUpperCase();

            return `
                <div class="history-item">
                    <div class="history-top">
                        <span class="pill">${status}</span>
                        <span class="history-amount">${formatCurrency(amount)}</span>
                    </div>
                    <div class="history-details">
                        <strong>${transactionReference}</strong>
                        <span>${date}</span>
                    </div>
                    <div class="history-actions">
                        <a class="history-link" href="${receiptUrl}">View receipt</a>
                    </div>
                </div>
            `;
        }).join("");
    }

    function renderLatestReceipt(item = readPaymentHistory()[0] || null) {
        if (!latestReceiptCard) return;

        if (!item) {
            latestReceiptCard.innerHTML = '<div class="empty-state small">Your latest receipt will appear here after a successful payment.</div>';
            return;
        }

        const amount = item.amount || item.total || FEE_AMOUNT;
        const receiptUrl = item.receiptUrl || `receipt.html?transactionId=${item.transactionId || item.id || ""}`;
        const transactionReference = item.transactionReference || item.reference || "Payment";
        const date = item.transactionDate ? new Date(item.transactionDate).toLocaleString("en-IN", {
            dateStyle: "medium",
            timeStyle: "short"
        }) : "—";

        latestReceiptCard.innerHTML = `
            <div class="receipt-summary-header">
                <div>
                    <span class="summary-label">Latest Receipt</span>
                    <h4>${transactionReference}</h4>
                </div>
                <span class="summary-amount">${formatCurrency(amount)}</span>
            </div>
            <div class="receipt-summary-meta">
                <span>Transaction ID: ${item.transactionId || item.id || "—"}</span>
                <span>Payment Date: ${date}</span>
            </div>
            <a class="btn btn-secondary btn-sm" href="${receiptUrl}">Open receipt</a>
        `;
    }

    async function loadStudentPaymentHistory() {
        const fallback = readPaymentHistory();
        renderPaymentHistory(fallback);
        renderLatestReceipt(fallback[0] || null);

        try {
            const response = await fetch(`/api/payments/history/student/${STUDENT_ID}`);
            if (!response.ok) return;

            const history = await response.json();
            if (Array.isArray(history) && history.length) {
                const normalized = history.map((item) => ({
                    transactionId: item.transactionId,
                    paymentId: item.paymentId,
                    transactionReference: item.transactionReference,
                    transactionStatus: item.transactionStatus,
                    amount: item.amount,
                    transactionDate: item.transactionDate,
                    receiptUrl: item.receiptUrl || `receipt.html?transactionId=${item.transactionId}`
                }));
                writePaymentHistory(normalized);
            }
        } catch (error) {
            console.warn("Unable to sync payment history from backend.", error);
        }
    }

    function saveLatestPaymentRecord(record) {
        const history = readPaymentHistory();
        const normalizedRecord = {
            transactionId: record.transactionId,
            paymentId: record.paymentId,
            transactionReference: record.transactionReference,
            transactionStatus: record.transactionStatus || "SUCCESS",
            amount: record.amount || FEE_AMOUNT,
            transactionDate: record.transactionDate || new Date().toISOString(),
            receiptUrl: record.receiptUrl || `receipt.html?transactionId=${record.transactionId}`
        };

        const filtered = history.filter((item) => String(item.transactionId || item.id) !== String(record.transactionId));
        filtered.unshift(normalizedRecord);
        writePaymentHistory(filtered.slice(0, 8));
    }

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

    if (showLoginLink) {
        showLoginLink.addEventListener("click", (e) => {
            e.preventDefault();
            signupView.classList.add("hidden");
            loginView.classList.remove("hidden");
            if (signupError) signupError.classList.add("hidden");
        });
    }

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

    if (loginForm) {
        loginForm.addEventListener("submit", (e) => {
            e.preventDefault();
            const enteredUser = usernameInput.value.trim().toLowerCase();
            const enteredPass = passwordInput.value.trim();

            const foundUser = usersDatabase[enteredUser];

            if ((enteredUser === "student" && enteredPass === "password") || (foundUser && foundUser.password === enteredPass)) {
                currentUser = foundUser || usersDatabase.student;
                if (loginError) loginError.classList.add("hidden");
                if (signupSuccessBanner) signupSuccessBanner.classList.add("hidden");

                if (studentNameElem) studentNameElem.textContent = currentUser.name;
                if (studentRoleElem) studentRoleElem.textContent = `ID: ${currentUser.id} • Computer Science`;

                if (avatarElem) {
                    const initials = currentUser.name.split(" ").map((n) => n[0]).join("").toUpperCase().slice(0, 2);
                    avatarElem.textContent = initials || "AM";
                }

                loginView.classList.add("hidden");
                dashboardView.classList.remove("hidden");
                loadStudentPaymentHistory();
            } else {
                if (loginError) loginError.classList.remove("hidden");
            }
        });
    }

    if (logoutBtn) {
        logoutBtn.addEventListener("click", () => {
            dashboardView.classList.add("hidden");
            loginView.classList.remove("hidden");
            if (passwordInput) passwordInput.value = "";
            clearSessionPaymentData();
            currentUser = null;
        });
    }

    methodCards.forEach((card) => {
        card.addEventListener("click", () => {
            methodCards.forEach((item) => item.classList.remove("active"));
            card.classList.add("active");

            const radio = card.querySelector('input[type="radio"]');
            if (radio) radio.checked = true;
        });
    });

    if (payNowBtn) {
        payNowBtn.addEventListener("click", async () => {
            const selected = document.querySelector('input[name="payMethod"]:checked');
            const paymentMethod = selected ? selected.value : "card";

            payNowBtn.disabled = true;
            if (paySpinner) paySpinner.classList.remove("hidden");
            if (payBtnText) payBtnText.textContent = "Connecting to Razorpay...";

            try {
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
                                sessionStorage.setItem(STORAGE_KEYS.lastTransactionId, String(verifyData.transactionId));
                                sessionStorage.setItem(STORAGE_KEYS.lastPaymentId, String(verifyData.razorpayPaymentId || ""));
                                sessionStorage.setItem(STORAGE_KEYS.lastTransactionReference, verifyData.transactionReference || "");
                                sessionStorage.setItem(STORAGE_KEYS.paymentMethod, paymentMethod);
                                sessionStorage.removeItem(STORAGE_KEYS.paymentError);

                                saveLatestPaymentRecord({
                                    transactionId: verifyData.transactionId,
                                    paymentId: verifyData.razorpayPaymentId,
                                    transactionReference: verifyData.transactionReference,
                                    transactionStatus: "SUCCESS",
                                    amount: verifyData.amount || FEE_AMOUNT,
                                    transactionDate: verifyData.transactionDate || new Date().toISOString(),
                                    receiptUrl: `receipt.html?transactionId=${verifyData.transactionId}`
                                });

                                window.location.href = "payment-success.html?transactionId=" + encodeURIComponent(verifyData.transactionId);
                            } else {
                                sessionStorage.setItem(STORAGE_KEYS.paymentError, verifyData.message || "Payment verification failed.");
                                window.location.href = "payment-failed.html";
                            }
                        } catch (err) {
                            sessionStorage.setItem(STORAGE_KEYS.paymentError, err.message || "Error verifying payment with server.");
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
                    sessionStorage.setItem(STORAGE_KEYS.paymentError, errorDesc);
                    window.location.href = "payment-failed.html";
                });

                rzp.open();
            } catch (error) {
                payNowBtn.disabled = false;
                if (paySpinner) paySpinner.classList.add("hidden");
                if (payBtnText) payBtnText.textContent = "Pay ₹5,050.00 Now";
                sessionStorage.setItem(STORAGE_KEYS.paymentError, error.message || "Could not launch Razorpay checkout.");
                window.location.href = "payment-failed.html";
            }
        });
    }
});
