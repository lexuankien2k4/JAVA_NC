
// Lấy tham chiếu đến các phần tử DOM
const loginFormSection = document.getElementById('loginFormSection');
const registerFormSection = document.getElementById('registerFormSection');
const userInfoSection = document.getElementById('userInfoSection');

const loginForm = document.getElementById('loginForm');
const registerForm = document.getElementById('registerForm');

const loginMessage = document.getElementById('loginMessage');
const registerMessage = document.getElementById('registerMessage');
const protectedDataMessage = document.getElementById('protectedDataMessage');
const authenticatedUsernameSpan = document.getElementById('authenticatedUsername');


// --- Hàm hiển thị/ẩn các khu vực giao diện ---
function showRegisterForm() {
    loginFormSection.style.display = 'none';
    registerFormSection.style.display = 'block';
    userInfoSection.style.display = 'none'; // Ẩn khu vực user info
    clearMessages(); // Xóa các thông báo cũ
}

function showLoginForm() {
    loginFormSection.style.display = 'block';
    registerFormSection.style.display = 'none';
    userInfoSection.style.display = 'none'; // Ẩn khu vực user info
    clearMessages(); // Xóa các thông báo cũ
}

function showUserInfo(username) {
    loginFormSection.style.display = 'none';
    registerFormSection.style.display = 'none';
    userInfoSection.style.display = 'block';
    authenticatedUsernameSpan.textContent = username; // Hiển thị tên người dùng
    clearMessages(); // Xóa các thông báo cũ
}

// --- Hàm xóa tất cả thông báo ---
function clearMessages() {
    loginMessage.textContent = '';
    loginMessage.className = 'message';
    registerMessage.textContent = '';
    registerMessage.className = 'message';
    protectedDataMessage.textContent = '';
    protectedDataMessage.className = 'message';
}

// --- Hàm hiển thị thông báo (thành công/lỗi) ---
function displayMessage(element, msg, isSuccess = true) {
    element.textContent = msg;
    element.className = isSuccess ? 'message success' : 'message error';
}

// --- Xử lý sự kiện gửi form Đăng ký ---
registerForm.addEventListener('submit', async (e) => {
    e.preventDefault(); // Ngăn chặn hành động submit mặc định của form (tải lại trang)
    clearMessages(); // Xóa thông báo cũ

    const username = document.getElementById('registerUsername').value;
    const password = document.getElementById('registerPassword').value;
    // Lấy thêm các trường khác nếu có (ví dụ: email = document.getElementById('registerEmail').value;)

    try {
        // --- Bước 1: Gửi yêu cầu Đăng ký đến Backend ---
        const response = await fetch('/api/v1/auth/register', { // URL endpoint đăng ký
            method: 'POST', // Phương thức POST
            headers: {
                'Content-Type': 'application/json', // Loại nội dung là JSON
            },
            body: JSON.stringify({ username, password }), // Chuyển dữ liệu thành chuỗi JSON và gửi đi
        });

        // --- Bước 2: Xử lý phản hồi từ Backend ---
        // Backend trả về ResponseData<AuthenticationResponse> hoặc ResponseData<String/Object khác>
        const responseData = await response.json();

        if (response.ok) { // response.ok là true nếu HTTP status code là 2xx
            displayMessage(registerMessage, responseData.message || "Đăng ký thành công!", true);

            // Nếu Backend tự động đăng nhập sau đăng ký và trả về token
            if (responseData.data && responseData.data.accessToken) {
                 // Lưu token
                 localStorage.setItem('accessToken', responseData.data.accessToken);
                 localStorage.setItem('refreshToken', responseData.data.refreshToken); // Lưu refresh token nếu có
                 // Lưu username để hiển thị sau (Backend nên trả về UserResponse chứa username)
                 const authenticatedUsername = responseData.data.userResponse?.username || username;
                 localStorage.setItem('authenticatedUsername', authenticatedUsername);

                 console.log("Đăng ký thành công & Tự động đăng nhập. Token đã lưu.");
                 // Chuyển sang màn hình thông tin người dùng sau 1.5 giây
                 setTimeout(() => showUserInfo(authenticatedUsername), 1500);
            } else {
                // Nếu chỉ trả về thông báo thành công, chuyển về form đăng nhập
                console.log("Đăng ký thành công. Vui lòng đăng nhập.");
                setTimeout(showLoginForm, 2000); // Chuyển sau 2 giây
            }

        } else { // Nếu HTTP status code là 4xx hoặc 5xx (lỗi)
            // Backend trả về tin nhắn lỗi trong trường message của ResponseData
            displayMessage(registerMessage, responseData.message || 'Đăng ký thất bại.', false);
             console.error("Lỗi đăng ký:", responseData.message || 'Phản hồi lỗi từ server.');
        }
    } catch (error) {
        // Xử lý lỗi mạng hoặc lỗi request
        console.error('Lỗi khi gửi yêu cầu đăng ký:', error);
        displayMessage(registerMessage, 'Có lỗi xảy ra khi kết nối đến máy chủ.', false);
    }
});

// --- Xử lý sự kiện gửi form Đăng nhập ---
loginForm.addEventListener('submit', async (e) => {
    e.preventDefault(); // Ngăn chặn tải lại trang
    clearMessages(); // Xóa thông báo cũ

    const username = document.getElementById('loginUsername').value;
    const password = document.getElementById('loginPassword').value;

    try {
        // --- Bước 1: Gửi yêu cầu Đăng nhập đến Backend ---
        const response = await fetch('/api/v1/auth', { // URL endpoint đăng nhập (POST không có path trong Controller)
            method: 'POST', // Phương thức POST
            headers: {
                'Content-Type': 'application/json', // Loại nội dung là JSON
            },
            body: JSON.stringify({ username, password }), // Chuyển dữ liệu thành chuỗi JSON và gửi đi
        });

        // --- Bước 2: Xử lý phản hồi từ Backend ---
        // Backend trả về ResponseData<AuthenticationResponse>
        const responseData = await response.json();

        if (response.ok) { // response.ok là true nếu HTTP status code là 2xx
            // Backend trả về AuthenticationResponse trong trường data
            const authResponse = responseData.data;

            if (authResponse && authResponse.accessToken) {
                 displayMessage(loginMessage, "Đăng nhập thành công!", true);

                 // --- Bước 3: Lưu Access Token và Refresh Token ---
                 localStorage.setItem('accessToken', authResponse.accessToken); // Lưu Access Token
                 localStorage.setItem('refreshToken', authResponse.refreshToken); // Lưu Refresh Token (nếu có)
                 // Lưu username để hiển thị sau (Backend nên trả về UserResponse chứa username)
                 const authenticatedUsername = authResponse.userResponse?.username || username;
                 localStorage.setItem('authenticatedUsername', authenticatedUsername);

                 console.log("Đăng nhập thành công. Access Token và Refresh Token đã lưu.");

                 // --- Bước 4: Cập nhật giao diện sau khi đăng nhập thành công ---
                 // Chuyển sang màn hình thông tin người dùng sau 1.5 giây
                 setTimeout(() => showUserInfo(authenticatedUsername), 1500);

            } else {
                 // Trường hợp thành công nhưng phản hồi không chứa token
                 displayMessage(loginMessage, responseData.message || 'Đăng nhập thành công nhưng không nhận được token.', false);
                 console.error("Đăng nhập thành công nhưng không có token:", responseData);
            }

        } else { // Nếu HTTP status code là 4xx hoặc 5xx (lỗi)
            // Backend trả về tin nhắn lỗi (ví dụ: tên người dùng/mật khẩu sai)
            displayMessage(loginMessage, responseData.message || 'Đăng nhập thất bại. Vui lòng kiểm tra lại tên người dùng và mật khẩu.', false);
            console.error("Lỗi đăng nhập:", responseData.message || 'Phản hồi lỗi từ server.');
        }
    } catch (error) {
        // Xử lý lỗi mạng hoặc lỗi request
        console.error('Lỗi khi gửi yêu cầu đăng nhập:', error);
        displayMessage(loginMessage, 'Có lỗi xảy ra khi kết nối đến máy chủ.', false);
    }
});

// --- Xử lý Đăng xuất ---
function logout() {
    // --- Bước 5: Xóa Token khi Đăng xuất ---
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken'); // Xóa refresh token
     localStorage.removeItem('authenticatedUsername'); // Xóa cả username đã lưu

    console.log("Đã xóa Access Token, Refresh Token và Username.");
    // Quay lại form đăng nhập
    showLoginForm();
    displayMessage(loginMessage, "Bạn đã đăng xuất.", true);
}


// --- Ví dụ gọi API cần xác thực (Sử dụng Access Token) ---
async function accessProtectedData() {
    clearMessages(); // Xóa thông báo cũ
    const accessToken = localStorage.getItem('accessToken'); // Lấy Access Token đã lưu

    // --- Bước 6: Kiểm tra Token trước khi gọi API bảo vệ ---
    if (!accessToken) {
        displayMessage(protectedDataMessage, "Bạn cần đăng nhập để truy cập dữ liệu này.", false);
        console.log("Không có Access Token trong Local Storage.");
        // Có thể chuyển hướng hoặc yêu cầu đăng nhập lại
        // logout(); // Đăng xuất nếu không có token
        return;
    }

    try {
        // --- Bước 7: Gửi yêu cầu đến Endpoint Bảo vệ kèm Access Token ---
        const response = await fetch('/api/v1/protected-resource', { // <-- Thay thế bằng URL endpoint bảo vệ thực tế của bạn
            method: 'GET', // hoặc POST, PUT, DELETE tùy API
            headers: {
                // Thêm header Authorization với định dạng "Bearer [token]"
                'Authorization': `Bearer ${accessToken}`,
                'Content-Type': 'application/json' // Thêm nếu API yêu cầu JSON body
            },
             // body: JSON.stringify({}) // Thêm body nếu phương thức là POST/PUT/PATCH
        });

        // --- Bước 8: Xử lý phản hồi từ Endpoint Bảo vệ ---
        const responseData = await response.json(); // Giả định API bảo vệ cũng trả về ResponseData

        if (response.ok) { // Nếu request thành công (status 2xx)
            displayMessage(protectedDataMessage, "Truy cập dữ liệu bảo mật thành công!", true);
            console.log("Dữ liệu bảo mật nhận được:", responseData.data); // Dữ liệu thực tế nằm trong trường data
        } else if (response.status === 401 || response.status === 403) {
            // --- Bước 9: Xử lý khi Access Token hết hạn hoặc không hợp lệ ---
            displayMessage(protectedDataMessage, responseData.message || "Phiên đăng nhập đã hết hạn hoặc không có quyền truy cập. Vui lòng đăng nhập lại.", false);
            console.warn("Truy cập bị từ chối:", response.status, responseData.message);

            // *** Bước 10: Tự động thử làm mới Token ***
            tryRefreshToken().then(success => {
                if (success) {
                    // Nếu làm mới thành công, thử gọi lại API bảo vệ ngay lập tức
                    console.log("Access Token đã được làm mới, đang thử gọi lại API bảo vệ...");
                    accessProtectedData(); // Gọi lại hàm truy cập dữ liệu
                } else {
                    // Nếu làm mới thất bại (refresh token cũng hết hạn/không hợp lệ), yêu cầu đăng nhập lại
                     console.log("Làm mới token thất bại, yêu cầu đăng nhập lại.");
                     logout(); // Xóa token và quay về trang login
                }
            });

        } else { // Xử lý các mã lỗi HTTP khác từ API bảo vệ
             displayMessage(protectedDataMessage, responseData.message || `Lỗi ${response.status} khi truy cập dữ liệu bảo mật.`, false);
             console.error(`Lỗi HTTP ${response.status}:`, responseData.message || 'Lỗi không xác định.');
        }

    } catch (error) {
        // Xử lý lỗi mạng hoặc lỗi request khi gọi API bảo vệ
        console.error('Lỗi khi truy cập dữ liệu bảo mật:', error);
        displayMessage(protectedDataMessage, 'Có lỗi xảy ra khi kết nối đến máy chủ.', false);
    }
}

// --- Hàm thử làm mới token (Sử dụng Refresh Token) ---
// Hàm này được gọi khi Access Token hết hạn (sau khi nhận 401/403)
async function tryRefreshToken() {
     const refreshToken = localStorage.getItem('refreshToken'); // Lấy Refresh Token

     // --- Bước 11: Kiểm tra Refresh Token trước khi gửi yêu cầu làm mới ---
     if (!refreshToken) {
         console.log("Không tìm thấy Refresh Token để làm mới.");
         return false; // Không có refresh token để làm mới
     }

     console.log("Đang thử làm mới Access Token bằng Refresh Token...");

     try {
         // --- Bước 12: Gửi yêu cầu đến Endpoint Refresh Token Backend ---
         const response = await fetch('/api/v1/auth/refresh-token', { // URL endpoint refresh token
             method: 'POST', // Phương thức POST
             headers: {
                 'Content-Type': 'application/json', // Loại nội dung là JSON
             },
             body: JSON.stringify({ refreshToken: refreshToken }), // Gửi refresh token trong body request
         });

         // --- Bước 13: Xử lý phản hồi từ Endpoint Refresh Token ---
         // Backend trả về ResponseData<AccessTokenResponse>
         const responseData = await response.json();

         if (response.ok) { // Nếu thành công (status 2xx)
             const accessTokenResponse = responseData.data; // Backend trả về AccessTokenResponse

             if (accessTokenResponse && accessTokenResponse.accessToken) {
                 // --- Bước 14: Lưu Access Token MỚI ---
                 localStorage.setItem('accessToken', accessTokenResponse.accessToken); // Lưu Access Token MỚI thay thế cái cũ

                 // Nếu Backend refresh token cũng trả về cả refresh token mới, lưu nó lại:
                 // if (accessTokenResponse.refreshToken) {
                 //      localStorage.setItem('refreshToken', accessTokenResponse.refreshToken);
                 // }

                 console.log("Access Token đã được làm mới thành công.");
                 return true; // Làm mới thành công
             }
         }
         // --- Bước 15: Xử lý khi Refresh Token không hợp lệ/hết hạn ---
          console.log("Làm mới token thất bại hoặc không nhận được Access Token mới:", responseData.message);
          return false; // Làm mới thất bại

     } catch (error) {
         // Xử lý lỗi mạng hoặc lỗi request khi gọi API làm mới
         console.error('Lỗi khi gửi yêu cầu làm mới token:', error);
         return false; // Làm mới thất bại do lỗi kết nối
     }
}


// --- Logic kiểm tra trạng thái đăng nhập khi tải trang ---
document.addEventListener('DOMContentLoaded', () => {
    const accessToken = localStorage.getItem('accessToken');
    const storedUsername = localStorage.getItem('authenticatedUsername'); // Cần lưu username khi login/register thành công

    if (accessToken && storedUsername) {
        // Nếu có Access Token và username đã lưu, hiển thị khu vực thông tin người dùng
        // (Giả định token vẫn còn giá trị hoặc sẽ được làm mới tự động nếu hết hạn)
        showUserInfo(storedUsername);
    } else {
        // Nếu không có token hoặc username, hiển thị form đăng nhập mặc định
        showLoginForm();
    }
});