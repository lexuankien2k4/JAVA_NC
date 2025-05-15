const loginFormSection = document.getElementById('loginFormSection');
const registerFormSection = document.getElementById('registerFormSection');
const userInfoSection = document.getElementById('userInfoSection');

const loginForm = document.getElementById('loginForm');
const registerForm = document.getElementById('registerForm');

const loginMessage = document.getElementById('loginMessage');
const registerMessage = document.getElementById('registerMessage');
const protectedDataMessage = document.getElementById('protectedDataMessage');
const authenticatedUsernameSpan = document.getElementById('authenticatedUsername');


// --- Hàm hiển thị form ---
function showRegisterForm() {
    loginFormSection.style.display = 'none';
    registerFormSection.style.display = 'block';
    userInfoSection.style.display = 'none'; // Hide user info section
    clearMessages();
}

function showLoginForm() {
    loginFormSection.style.display = 'block';
    registerFormSection.style.display = 'none';
    userInfoSection.style.display = 'none'; // Hide user info section
    clearMessages();
}

function showUserInfo(username) {
    loginFormSection.style.display = 'none';
    registerFormSection.style.display = 'none';
    userInfoSection.style.display = 'block';
    authenticatedUsernameSpan.textContent = username;
     clearMessages();
}

function clearMessages() {
    loginMessage.textContent = '';
    loginMessage.className = 'message';
    registerMessage.textContent = '';
    registerMessage.className = 'message';
    protectedDataMessage.textContent = '';
     protectedDataMessage.className = 'message';
}

// --- Hàm xử lý tin nhắn ---
function displayMessage(element, msg, isSuccess = true) {
    element.textContent = msg;
    element.className = isSuccess ? 'message success' : 'message error';
}

// --- Xử lý Đăng ký ---
registerForm.addEventListener('submit', async (e) => {
    e.preventDefault(); // Ngăn chặn tải lại trang
    clearMessages();

    const username = document.getElementById('registerUsername').value;
    const password = document.getElementById('registerPassword').value;
    // Lấy thêm các trường khác nếu có

    try {
        // Gửi yêu cầu POST đến endpoint đăng ký backend
        const response = await fetch('/api/v1/auth/register', { // Đảm bảo URL đúng với Spring Boot Controller
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ username, password }), // Gửi dữ liệu dưới dạng JSON
        });

        // Phân tích phản hồi JSON từ backend (dạng ResponseData<AuthenticationResponse>)
        const responseData = await response.json();

        if (response.ok) { // Nếu response.status là 2xx (thành công)
            // Backend trả về AuthenticationResponse nếu tự động đăng nhập sau đăng ký
            if (responseData.data && responseData.data.accessToken) {
                 displayMessage(registerMessage, "Đăng ký thành công! Đang tự động đăng nhập...", true);
                 // Lưu token và hiển thị thông tin người dùng
                 localStorage.setItem('accessToken', responseData.data.accessToken);
                 localStorage.setItem('refreshToken', responseData.data.refreshToken); // Lưu refresh token nếu có
                 // Assume the backend returns username in userResponse part of AuthenticationResponse
                 const authenticatedUsername = responseData.data.userResponse?.username || username; // Use returned username if available, else use input username
                 setTimeout(() => showUserInfo(authenticatedUsername), 1500); // Chuyển sang màn hình thông tin sau 1.5 giây
            } else {
                // Nếu backend chỉ trả về tin nhắn thành công mà không tự động đăng nhập
                displayMessage(registerMessage, responseData.message || "Đăng ký thành công!", true);
                setTimeout(showLoginForm, 2000); // Chuyển sang form đăng nhập sau 2 giây
            }

        } else { // Nếu response.status là 4xx hoặc 5xx (lỗi)
            // Backend trả về tin nhắn lỗi trong trường message của ResponseData
            displayMessage(registerMessage, responseData.message || 'Đăng ký thất bại.', false);
        }
    } catch (error) {
        console.error('Lỗi khi gửi yêu cầu đăng ký:', error);
        displayMessage(registerMessage, 'Có lỗi xảy ra khi kết nối đến máy chủ.', false);
    }
});

// --- Xử lý Đăng nhập ---
loginForm.addEventListener('submit', async (e) => {
    e.preventDefault(); // Ngăn chặn tải lại trang
    clearMessages();

    const username = document.getElementById('loginUsername').value;
    const password = document.getElementById('loginPassword').value;

    try {
        // Gửi yêu cầu POST đến endpoint đăng nhập backend
        const response = await fetch('/api/v1/auth', { // Đảm bảo URL đúng với Spring Boot Controller (@PostMapping không có path)
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ username, password }), // Gửi dữ liệu dưới dạng JSON
        });

         // Phân tích phản hồi JSON từ backend (dạng ResponseData<AuthenticationResponse>)
        const responseData = await response.json();


        if (response.ok) { // Nếu response.status là 2xx (thành công)
            // Backend trả về AuthenticationResponse trong trường data
            const authResponse = responseData.data;
            if (authResponse && authResponse.accessToken) {
                 displayMessage(loginMessage, "Đăng nhập thành công!", true);
                 // Lưu Access Token và Refresh Token vào Local Storage
                 localStorage.setItem('accessToken', authResponse.accessToken);
                 localStorage.setItem('refreshToken', authResponse.refreshToken); // Lưu refresh token
                 console.log("Đã lưu Access Token và Refresh Token");

                 // Hiển thị thông tin người dùng đã đăng nhập
                 // Assume the backend returns username in userResponse part of AuthenticationResponse
                 const authenticatedUsername = authResponse.userResponse?.username || username; // Use returned username if available, else use input username
                 setTimeout(() => showUserInfo(authenticatedUsername), 1500); // Chuyển sang màn hình thông tin sau 1.5 giây

            } else {
                 displayMessage(loginMessage, responseData.message || 'Đăng nhập thành công nhưng không nhận được token.', false);
            }

        } else { // Nếu response.status là 4xx hoặc 5xx (lỗi)
             // Backend trả về tin nhắn lỗi trong trường message của ResponseData
            displayMessage(loginMessage, responseData.message || 'Đăng nhập thất bại.', false);
        }
    } catch (error) {
        console.error('Lỗi khi gửi yêu cầu đăng nhập:', error);
        displayMessage(loginMessage, 'Có lỗi xảy ra khi kết nối đến máy chủ.', false);
    }
});

// --- Xử lý Đăng xuất ---
function logout() {
    // Xóa token khỏi Local Storage
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    console.log("Đã xóa Access Token và Refresh Token");
    // Quay lại form đăng nhập
    showLoginForm();
    displayMessage(loginMessage, "Bạn đã đăng xuất.", true);
}


// --- Ví dụ gọi API cần xác thực ---
async function accessProtectedData() {
    clearMessages();
    const accessToken = localStorage.getItem('accessToken');

    if (!accessToken) {
        displayMessage(protectedDataMessage, "Bạn cần đăng nhập để truy cập dữ liệu này.", false);
        return;
    }

    try {
        // Gửi yêu cầu GET đến một endpoint bảo vệ, kèm theo Access Token trong header
        const response = await fetch('/api/v1/protected-resource', { // Thay thế bằng URL endpoint bảo vệ thực tế của bạn
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${accessToken}` // Đính kèm Access Token
            },
        });

        // Endpoint bảo vệ của backend có thể trả về ResponseData<AnyType>
        const responseData = await response.json();


        if (response.ok) { // Nếu response.status là 2xx (thành công)
            displayMessage(protectedDataMessage, "Truy cập dữ liệu bảo mật thành công!", true);
            console.log("Dữ liệu bảo mật nhận được:", responseData.data); // Log dữ liệu nhận được
        } else if (response.status === 401 || response.status === 403) {
             // Xử lý khi token hết hạn hoặc không hợp lệ (JWTAuthenticationFilter sẽ trả về 401/403)
            displayMessage(protectedDataMessage, responseData.message || "Phiên đăng nhập đã hết hạn hoặc không có quyền truy cập. Vui lòng đăng nhập lại.", false);
            // Tự động làm mới token hoặc yêu cầu đăng nhập lại
            // Ví dụ đơn giản: xóa token và quay lại form login
             localStorage.removeItem('accessToken');
             // Có thể thử dùng refresh token ở đây trước khi xóa refresh token và yêu cầu login lại
             // tryRefreshToken().then(success => { if (!success) logout(); });
             logout(); // Trong ví dụ đơn giản này, chúng ta đăng xuất luôn
        }
        else {
             displayMessage(protectedDataMessage, responseData.message || 'Không thể truy cập dữ liệu bảo mật.', false);
        }

    } catch (error) {
        console.error('Lỗi khi truy cập dữ liệu bảo mật:', error);
        displayMessage(protectedDataMessage, 'Có lỗi xảy ra khi kết nối đến máy chủ.', false);
    }
}

// --- Hàm thử làm mới token (Cần endpoint /api/v1/auth/refresh-token hoạt động) ---
async function tryRefreshToken() {
     const refreshToken = localStorage.getItem('refreshToken');
     if (!refreshToken) {
         console.log("Không tìm thấy Refresh Token.");
         return false;
     }

     console.log("Đang thử làm mới Access Token bằng Refresh Token...");

     try {
         const response = await fetch('/api/v1/auth/refresh-token', { // URL endpoint refresh token
             method: 'POST',
             headers: {
                 'Content-Type': 'application/json',
             },
             body: JSON.stringify({ refreshToken: refreshToken }), // Gửi refresh token trong body
         });

         const responseData = await response.json();

         if (response.ok) { // Nếu thành công
             const accessTokenResponse = responseData.data; // Backend trả về AccessTokenResponse
             if (accessTokenResponse && accessTokenResponse.accessToken) {
                 localStorage.setItem('accessToken', accessTokenResponse.accessToken); // Lưu Access Token MỚI
                 console.log("Access Token đã được làm mới thành công.");
                 // Có thể gọi lại hàm accessProtectedData() ngay sau khi làm mới thành công
                 // accessProtectedData();
                 return true; // Làm mới thành công
             }
         }
          console.log("Làm mới token thất bại hoặc không nhận được Access Token mới:", responseData.message);
          return false; // Làm mới thất bại

     } catch (error) {
         console.error('Lỗi khi gửi yêu cầu làm mới token:', error);
         return false; // Làm mới thất bại
     }
}


// --- Kiểm tra trạng thái đăng nhập khi tải trang ---
// Nếu có token trong localStorage, hiển thị thông tin người dùng
document.addEventListener('DOMContentLoaded', () => {
    const accessToken = localStorage.getItem('accessToken');
    // Bạn có thể cần gọi API để lấy lại thông tin người dùng đầy đủ nếu chỉ lưu token
    // hoặc lưu username vào localStorage khi đăng nhập/đăng ký thành công
    const storedUsername = localStorage.getItem('authenticatedUsername'); // Cần lưu username khi đăng nhập/đky

    if (accessToken && storedUsername) {
        // Giả định token còn hiệu lực (hoặc bạn có thể thử xác minh nó ở frontend hoặc gọi API kiểm tra)
        showUserInfo(storedUsername);
    } else {
        showLoginForm(); // Mặc định hiển thị form đăng nhập
    }
});

// Cập nhật hàm xử lý đăng nhập/đăng ký để lưu username vào localStorage
// Ví dụ trong login success:
// localStorage.setItem('authenticatedUsername', authenticatedUsername); // Thêm dòng này

// Ví dụ trong register success khi tự động login:
// localStorage.setItem('authenticatedUsername', authenticatedUsername); // Thêm dòng này