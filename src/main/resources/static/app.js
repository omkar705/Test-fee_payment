document.addEventListener("DOMContentLoaded", () => {
    const loginView = document.getElementById("loginView");
    const dashboardView = document.getElementById("dashboardView");
    const loginForm = document.getElementById("loginForm");
    const loginError = document.getElementById("loginError");
    const usernameInput = document.getElementById("username");
    const passwordInput = document.getElementById("password");
    const logoutBtn = document.getElementById("logoutBtn");
    const payNowBtn = document.getElementById("payNowBtn");
    const payBtnText = document.getElementById("payBtnText");
    const paySpinner = document.getElementById("paySpinner");
    const methodCards = document.querySelectorAll(".method-card");

    const STATIC_USER = "student";
    const STATIC_PASS = "password";

    const PAYMENT_ID = 1;
    const STUDENT_ID = 1;
    const FEE_AMOUNT = 5050;

    // Login Form Submit
    if (loginForm) {
        loginForm.addEventListener("submit", (event) => {
            event.preventDefault();

            const username = usernameInput.value.trim();
            const password = passwordInput.value.trim();

            if (username === STATIC_USER && password === STATIC_PASS) {
                loginError.classList.add("hidden");
                loginView.classList.add("hidden");
                dashboardView.classList.remove("hidden");
            } else {
                loginError.classList.remove("hidden");
            }
        });
    }

    // Logout
    if (logoutBtn) {
        logoutBtn.addEventListener("click", () => {
            dashboardView.classList.add("hidden");
            loginView.classList.remove("hidden");
            passwordInput.value = "";
        });
    }

    // Payment Method Card Selection
    methodCards.forEach((card) => {
        card.addEventListener("click", () => {
            methodCards.forEach((item) => item.classList.remove("active"));
            card.classList.add("active");

            const radio = card.querySelector('input[type="radio"]');
            if (radio) radio.checked = true;
        });
    });

    // Pay Now Button - Trigger Official Razorpay Standard Checkout
    if (payNowBtn) {
        payNowBtn.addEventListener("click", async () => {
            const selected = document.querySelector('input[name="payMethod"]:checked');
            const paymentMethod = selected ? selected.value : "card";

            // Set loading state on button
            payNowBtn.disabled = true;
            if (paySpinner) paySpinner.classList.remove("hidden");
            if (payBtnText) payBtnText.textContent = "Connecting to Razorpay...";

            try {
                // 1. Request Order Creation from Backend
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

                // 2. Configure Official Razorpay Standard Checkout Modal
                const options = {
                    key: orderData.keyId,
                    amount: orderData.amountInPaise,
                    currency: orderData.currency || "INR",
                    name: "University Fee Portal",
                    description: "Student Fee Payment - Fall Semester 2026",
                    order_id: orderData.orderId,
                    image: "https://cdn.razorpay.com/static/assets/logo/rzp.png",
                    prefill: {
                        name: "Alex Morgan",
                        email: "alex.morgan@university.edu",
                        contact: "9876543210"
                    },
                    notes: {
                        student_id: String(STUDENT_ID),
                        payment_id: String(PAYMENT_ID),
                        purpose: "Tuition & Associated Fees"
                    },
                    theme: {
                        color: "#4361ee"
                    },
                    modal: {
                        ondismiss: function () {
                            payNowBtn.disabled = false;
                            if (paySpinner) paySpinner.classList.add("hidden");
                            if (payBtnText) payBtnText.textContent = "Pay ₹5,050.00 Now";
                        }
                    },
                    handler: async function (response) {
                        // 3. Payment completed in Razorpay modal -> Verify signature with backend
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

                // Check if Razorpay SDK loaded
                if (typeof Razorpay === "undefined") {
                    throw new Error("Razorpay Checkout SDK is not loaded. Please check your internet connection.");
                }

                // 4. Open Real Razorpay Checkout Modal
                const rzp = new Razorpay(options);

                rzp.on("payment.failed", function (response) {
                    payNowBtn.disabled = false;
                    if (paySpinner) paySpinner.classList.add("hidden");
                    if (payBtnText) payBtnText.textContent = "Pay ₹5,050.00 Now";

                    const errorDesc = (response && response.error && response.error.description) 
                        ? response.error.description 
                        : "Payment was declined or failed.";
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
