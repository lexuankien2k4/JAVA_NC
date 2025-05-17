// ----- QUẢN LÝ GIAO DIỆN CHUNG (Sidebar, Dark Mode) -----
const sideMenu = document.querySelector('aside');
const menuBtn = document.getElementById('menu-btn');
const closeBtn = document.getElementById('close-btn');
const darkMode = document.querySelector('.dark-mode');

if (menuBtn && sideMenu) {
    menuBtn.addEventListener('click', () => {
        sideMenu.style.display = 'block';
    });
}

if (closeBtn && sideMenu) {
    closeBtn.addEventListener('click', () => {
        sideMenu.style.display = 'none';
    });
}

if (darkMode) {
    darkMode.addEventListener('click', () => {
        document.body.classList.toggle('dark-mode-variables');
        darkMode.querySelector('span:nth-child(1)').classList.toggle('active');
        darkMode.querySelector('span:nth-child(2)').classList.toggle('active');
    });
}

// ----- QUẢN LÝ SẢN PHẨM (Client-side rendering and actions) -----
document.addEventListener('DOMContentLoaded', () => {
    const productTableBody = document.getElementById('productTableBody');
    // Chỉ thực thi code quản lý sản phẩm nếu bảng sản phẩm tồn tại trên trang
    if (!productTableBody) {
        // console.log("Không tìm thấy productTableBody, bỏ qua script quản lý sản phẩm.");
        return;
    }

    const API_BASE_URL = '/api/v1/products'; // Cập nhật: API endpoint cho sản phẩm (ví dụ: /api/v1/products)

    const prevPageButton = document.getElementById('prevPageButton');
    const nextPageButton = document.getElementById('nextPageButton');
    const currentPageDisplay = document.getElementById('currentPageDisplay');
    const messageDisplay = document.getElementById('messageDisplay');

    // Các nút Export/Import (nếu bạn muốn thêm chức năng sau này)
    // const exportButton = document.querySelector('.export');
    // const importButton = document.querySelector('.import');

    let currentPage = 0; // Trang hiện tại (bắt đầu từ 0 cho API)
    let pageSize = 10;   // Số sản phẩm trên mỗi trang

    async function fetchProducts(page = 0, size = 10) {
        if (!messageDisplay) { console.error("messageDisplay element not found"); return; }
        showMessage('Đang tải dữ liệu sản phẩm...', 'info', false);
        try {
            // Giả sử API trả về đối tượng Page<Product> hoặc cấu trúc tương tự có content và thông tin phân trang
            const response = await fetch(`${API_BASE_URL}?page=${page}&size=${size}`); // API có thể dùng 'page' và 'size'

            if (!response.ok) {
                const errorData = await response.json().catch(() => ({ message: `Lỗi HTTP ${response.status}. Không thể phân tích phản hồi lỗi.` }));
                throw new Error(errorData.message || `Lỗi HTTP ${response.status}`);
            }
            const pageData = await response.json(); // Mong đợi đối tượng Page từ Spring Boot (hoặc cấu trúc tương tự)
                                                 // ví dụ: { content: [], totalPages: 5, number: 0, size: 10, totalElements: 48 }

            // Kiểm tra cấu trúc dữ liệu trả về từ API (ví dụ: responseData là một đối tượng Page)
            if (!pageData || !pageData.content) {
                 console.error("API Response không hợp lệ hoặc không có trường 'content':", pageData);
                 showMessage('Không thể tải danh sách sản phẩm hoặc định dạng dữ liệu không đúng.', 'error');
                 displayProducts([]); // Hiển thị bảng rỗng
                 updatePaginationControls([], 0, 0, 0); // Cập nhật điều khiển phân trang với trạng thái rỗng/lỗi
                 return;
            }

            showMessage('Tải sản phẩm thành công!', 'success');
            displayProducts(pageData.content); // pageData.content là danh sách sản phẩm
            updatePaginationControls(pageData.content, pageData.number, pageData.totalPages, pageData.size);

        } catch (error) {
            console.error('Lỗi khi tải sản phẩm:', error);
            showMessage(`Lỗi khi tải sản phẩm: ${error.message}`, 'error');
            displayProducts([]);
            updatePaginationControls([], currentPage, 0, pageSize); // Giữ trang hiện tại, tổng số trang là 0
        }
    }

    function displayProducts(products) {
        productTableBody.innerHTML = ''; // Xóa các dòng cũ
        if (!products || products.length === 0) {
            const row = productTableBody.insertRow();
            const cell = row.insertCell();
            cell.colSpan = 10; // Số cột trong bảng của bạn (ID, Hình Ảnh, ..., Hành Động)
            cell.textContent = 'Không có sản phẩm nào để hiển thị.';
            cell.style.textAlign = 'center';
            return;
        }

        products.forEach(product => {
            const row = productTableBody.insertRow();
            row.insertCell().textContent = product.id;

            const imgCell = row.insertCell();
            const img = document.createElement('img');
            // Sử dụng URL hình ảnh từ API, nếu không có thì dùng ảnh placeholder
            img.src = product.imageUrl || 'https://via.placeholder.com/50?text=N/A';
            img.alt = product.name; // Giả sử API trả về `name` thay vì `productName` để nhất quán với Thymeleaf
            img.style.width = '50px'; // Thêm style cơ bản cho ảnh
            img.style.height = 'auto';
            img.classList.add('product-image'); // Thêm class để có thể tùy chỉnh CSS
            img.onerror = () => { img.src = 'https://via.placeholder.com/50?text=Lỗi ảnh'; }; // Xử lý khi ảnh lỗi
            imgCell.appendChild(img);

            // Nhất quán với các trường Thymeleaf có thể sử dụng (vd: product.name)
            row.insertCell().textContent = product.name || 'N/A';
            row.insertCell().textContent = product.price ? product.price.toLocaleString('vi-VN', { style: 'currency', currency: 'VND' }) : 'N/A';
            row.insertCell().textContent = product.stockQuantity !== undefined ? product.stockQuantity : 'N/A';
            row.insertCell().textContent = product.soldQuantity !== undefined ? product.soldQuantity : '0'; // Mặc định là 0 nếu không có
             // Giả định API trả về `categoryName` đã được làm phẳng, hoặc nếu API trả về object lồng nhau: product.category.name
            row.insertCell().textContent = product.categoryName || (product.category ? product.category.name : 'N/A');
            row.insertCell().textContent = product.manufacturingDate ? formatDate(product.manufacturingDate) : 'N/A';
            row.insertCell().textContent = product.expiryDate ? formatDate(product.expiryDate) : 'N/A';

            const actionsCell = row.insertCell();
            actionsCell.classList.add('action-buttons'); // Class để style các nút (nếu cần)

            const editLink = document.createElement('a');
            // Đường dẫn đến trang sửa sản phẩm, phù hợp với cấu trúc Thymeleaf/Spring MVC
            editLink.href = `/admin/products/edit/${product.id}`;
            editLink.classList.add('action-edit'); // Class giống như trong Thymeleaf để nhất quán CSS
            editLink.innerHTML = '<span class="material-icons-sharp" style="font-size:1.1em; vertical-align: middle;">edit</span> Sửa';


            const deleteButton = document.createElement('button');
            deleteButton.classList.add('action-delete'); // Class giống như trong Thymeleaf
            deleteButton.innerHTML = '<span class="material-icons-sharp" style="font-size:1.1em; vertical-align: middle;">delete</span> Xóa';
            deleteButton.onclick = () => deleteProduct(product.id);

            actionsCell.appendChild(editLink);
            actionsCell.appendChild(deleteButton);
        });
    }

    async function deleteProduct(productId) {
        if (!confirm(`Bạn có chắc chắn muốn xóa sản phẩm có ID: ${productId}?`)) {
            return;
        }
        if (!messageDisplay) { console.error("messageDisplay element not found"); return; }
        showMessage('Đang xóa sản phẩm...', 'info', false);
        try {
            const response = await fetch(`${API_BASE_URL}/${productId}`, { method: 'DELETE' });

            if (response.status === 204) { // Xóa thành công, không có nội dung trả về
                showMessage('Sản phẩm đã được xóa thành công!', 'success');
                fetchProducts(currentPage, pageSize); // Tải lại danh sách sản phẩm
                return;
            }

            // Nếu có nội dung trả về (ví dụ JSON message)
            const responseData = await response.json();
            if (!response.ok) { // Các lỗi khác (4xx, 5xx)
                throw new Error(responseData.message || `Lỗi khi xóa sản phẩm. Status: ${response.status}`);
            }

            // Xóa thành công và có message (ví dụ status 200 OK with body)
            showMessage(responseData.message || 'Sản phẩm đã được xóa thành công!', 'success');
            fetchProducts(currentPage, pageSize); // Tải lại danh sách sản phẩm

        } catch (error) {
            console.error('Lỗi khi xóa sản phẩm:', error);
            // Kiểm tra xem error.message có phải là do response.json() thất bại trên body rỗng không
            if (error instanceof SyntaxError && error.message.includes("Unexpected end of JSON input") && response && response.status === 204) {
                 showMessage('Sản phẩm đã được xóa thành công (không có nội dung phản hồi)!', 'success');
                 fetchProducts(currentPage, pageSize);
            } else {
                 showMessage(`Lỗi khi xóa sản phẩm: ${error.message}`, 'error');
            }
        }
    }

    function formatDate(dateString) {
        if (!dateString) return 'N/A';
        try {
            const date = new Date(dateString);
            if (isNaN(date.getTime())) {
                return dateString; // Trả về chuỗi gốc nếu không parse được (ví dụ nếu nó đã được định dạng sẵn)
            }
            // Định dạng ngày tháng theo dd/MM/yyyy
            const day = String(date.getDate()).padStart(2, '0');
            const month = String(date.getMonth() + 1).padStart(2, '0'); // Tháng trong JS là 0-indexed
            const year = date.getFullYear();
            return `${day}/${month}/${year}`;
        } catch (e) {
            console.warn("Lỗi định dạng ngày:", dateString, e);
            return dateString; // Trả về chuỗi gốc nếu có lỗi
        }
    }

    function updatePaginationControls(productsOnPage, pageNumber, totalPages, pSize) {
        if (!currentPageDisplay || !prevPageButton || !nextPageButton) {
            // console.warn("Một hoặc nhiều phần tử phân trang không tìm thấy.");
            return;
        }

        currentPage = pageNumber; // Cập nhật trang hiện tại từ phản hồi API
        pageSize = pSize;       // Cập nhật kích thước trang

        currentPageDisplay.textContent = `Trang: ${currentPage + 1} / ${totalPages}`;
        prevPageButton.disabled = (currentPage === 0);
        nextPageButton.disabled = (currentPage + 1 >= totalPages);

        // Tự động lùi trang nếu trang hiện tại trở nên trống sau khi xóa (và không phải là trang đầu)
        if (productsOnPage.length === 0 && currentPage > 0) {
            // console.log("Trang hiện tại trống, lùi về trang trước.");
            fetchProducts(currentPage - 1, pageSize);
        }
    }

    function showMessage(message, type = 'info', autoDismiss = true) {
        if (!messageDisplay) return;
        messageDisplay.textContent = message;
        messageDisplay.className = 'message-area'; // Reset class
        // Áp dụng class dựa trên type, ví dụ: 'success-message', 'error-message', 'info-message'
        // Các class này cần được định nghĩa trong file qlsp.css của bạn
        messageDisplay.classList.add(`${type}-message`);
        messageDisplay.classList.remove('hidden'); // Hiển thị thông báo

        if (type === 'info' && !messageDisplay.classList.contains('info-message-styled')) {
            // Fallback style nếu CSS chưa định nghĩa (chỉ áp dụng 1 lần)
            // messageDisplay.style.backgroundColor = '#e0e0f0';
            // messageDisplay.style.color = '#333';
            // messageDisplay.style.border = '1px solid #ccc';
            // messageDisplay.style.padding = '10px';
            // messageDisplay.style.marginBottom = '15px';
            // messageDisplay.classList.add('info-message-styled');
        }


        if (autoDismiss) {
            setTimeout(() => {
                messageDisplay.classList.add('hidden');
            }, 3500); // Ẩn sau 3.5 giây
        }
    }

    // Gán sự kiện cho các nút phân trang
    if (prevPageButton) {
        prevPageButton.addEventListener('click', () => {
            if (currentPage > 0) {
                fetchProducts(currentPage - 1, pageSize);
            }
        });
    }

    if (nextPageButton) {
        nextPageButton.addEventListener('click', () => {
             // Kiểm tra dựa trên totalPages (nếu API cung cấp) trước khi tăng
             // Điều này được xử lý bởi `updatePaginationControls` khi disable nút
            fetchProducts(currentPage + 1, pageSize);
        });
    }

    // Tải danh sách sản phẩm lần đầu khi trang được load
    fetchProducts(currentPage, pageSize);
});