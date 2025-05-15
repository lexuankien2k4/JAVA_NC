// ----- QUẢN LÝ GIAO DIỆN CHUNG (Sidebar, Dark Mode) -----
// (Giữ nguyên phần code này nếu qlsp.js cũng chịu trách nhiệm cho các chức năng chung này)
const sideMenu = document.querySelector('aside');
const menuBtn = document.getElementById('menu-btn');
const closeBtn = document.getElementById('close-btn');
const darkMode = document.querySelector('.dark-mode');

if (menuBtn && sideMenu) { // Thêm kiểm tra null cho sideMenu
    menuBtn.addEventListener('click', () => {
        sideMenu.style.display = 'block';
    });
}

if (closeBtn && sideMenu) { // Thêm kiểm tra null cho sideMenu
    closeBtn.addEventListener('click', () => {
        sideMenu.style.display = 'none';
    });
}

if (darkMode) {
    darkMode.addEventListener('click', () => {
        document.body.classList.toggle('dark-mode-variables'); // Giả sử class này được định nghĩa trong qlsp.css
        darkMode.querySelector('span:nth-child(1)').classList.toggle('active');
        darkMode.querySelector('span:nth-child(2)').classList.toggle('active');
    });
}

// ----- QUẢN LÝ SẢN PHẨM -----
document.addEventListener('DOMContentLoaded', () => {
    const productTableBody = document.getElementById('productTableBody');
    if (!productTableBody) {
        return; // Không phải trang quản lý sản phẩm
    }

    const API_BASE_URL = '/api/v1/product'; // API endpoint của bạn

    const prevPageButton = document.getElementById('prevPageButton');
    const nextPageButton = document.getElementById('nextPageButton');
    const currentPageDisplay = document.getElementById('currentPageDisplay');
    const messageDisplay = document.getElementById('messageDisplay');

    let currentPage = 0;
    let pageSize = 10; // Hoặc giá trị bạn muốn

    async function fetchProducts(page = 0, size = 10) {
        if (!messageDisplay) { console.error("messageDisplay element not found"); return; }
        showMessage('Đang tải dữ liệu sản phẩm...', 'info', false);
        try {
            const response = await fetch(`${API_BASE_URL}?pageNumber=${page}&pageSize=${size}`);
            if (!response.ok) {
                const errorData = await response.json().catch(() => ({ message: `Lỗi HTTP ${response.status}` }));
                throw new Error(errorData.message || `Lỗi HTTP ${response.status}`);
            }
            const responseData = await response.json();

            if (responseData.status !== 200 || !responseData.data) {
                console.error("API Response không hợp lệ:", responseData);
                showMessage(responseData.message || 'Không thể tải danh sách sản phẩm hoặc không có sản phẩm.', 'error');
                displayProducts([]);
                updatePaginationControls([], size);
                return;
            }

            showMessage('Tải sản phẩm thành công!', 'success');
            displayProducts(responseData.data);
            updatePaginationControls(responseData.data, size);

        } catch (error) {
            console.error('Lỗi khi tải sản phẩm:', error);
            showMessage(`Lỗi khi tải sản phẩm: ${error.message}`, 'error');
            displayProducts([]);
            updatePaginationControls([], size);
        }
    }

    function displayProducts(products) {
        productTableBody.innerHTML = '';
        if (!products || products.length === 0) {
            const row = productTableBody.insertRow();
            const cell = row.insertCell();
            cell.colSpan = 10; // Số cột trong bảng
            cell.textContent = 'Không có sản phẩm nào để hiển thị.';
            cell.style.textAlign = 'center';
            return;
        }

        products.forEach(product => {
            const row = productTableBody.insertRow();
            row.insertCell().textContent = product.id;

            const imgCell = row.insertCell();
            const img = document.createElement('img');
            img.src = product.imageUrl || 'https://via.placeholder.com/50?text=N/A';
            img.alt = product.productName;
            img.classList.add('product-image'); // Class này nên được định nghĩa trong qlsp.css hoặc inline style
            img.onerror = () => { img.src = 'https://via.placeholder.com/50?text=Error'; };
            imgCell.appendChild(img);

            row.insertCell().textContent = product.productName || 'N/A';
            row.insertCell().textContent = product.price ? product.price.toLocaleString('vi-VN') + ' VND' : 'N/A';
            row.insertCell().textContent = product.stockQuantity !== undefined ? product.stockQuantity : 'N/A';
            row.insertCell().textContent = product.soldQuantity !== undefined ? product.soldQuantity : 'N/A';
            row.insertCell().textContent = product.categoryName || 'N/A';
            row.insertCell().textContent = product.productionDate ? formatDate(product.productionDate) : 'N/A';
            row.insertCell().textContent = product.expiryDate ? formatDate(product.expiryDate) : 'N/A';

            const actionsCell = row.insertCell();
            actionsCell.classList.add('action-buttons'); // Class này nên được định nghĩa

            const editLink = document.createElement('a');
            editLink.href = `them_suaSP.html?id=${product.id}`; // **CẬP NHẬT ĐƯỜNG DẪN**
            editLink.classList.add('button-edit'); // Sử dụng class đã có trong HTML
            editLink.innerHTML = '<span class="material-icons-sharp" style="font-size:1.1em; vertical-align: middle;">edit</span>';


            const deleteButton = document.createElement('button');
            deleteButton.classList.add('button-danger'); // Sử dụng class đã có trong HTML
            deleteButton.innerHTML = '<span class="material-icons-sharp" style="font-size:1.1em; vertical-align: middle;">delete</span>';
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
            const responseData = await response.json();

            if (!response.ok || responseData.status !== 200 && responseData.status !== 204 ) { // 204 No Content cũng là thành công cho DELETE
                throw new Error(responseData.message || `Lỗi khi xóa sản phẩm. Status: ${response.status}`);
            }

            showMessage(responseData.data || responseData.message || 'Sản phẩm đã được xóa thành công!', 'success');
            fetchProducts(currentPage, pageSize);
        } catch (error) {
            console.error('Lỗi khi xóa sản phẩm:', error);
            showMessage(`Lỗi: ${error.message}`, 'error');
        }
    }

    function formatDate(dateString) {
        if (!dateString) return 'N/A';
        try {
            const date = new Date(dateString);
            // Kiểm tra xem date có hợp lệ không sau khi parse
            if (isNaN(date.getTime())) {
                return dateString; // Trả về chuỗi gốc nếu không parse được
            }
            const day = String(date.getDate()).padStart(2, '0');
            const month = String(date.getMonth() + 1).padStart(2, '0');
            const year = date.getFullYear();
            return `${day}/${month}/${year}`;
        } catch (e) {
            return dateString;
        }
    }

    function updatePaginationControls(productsOnPage, pSize) {
        if (!currentPageDisplay || !prevPageButton || !nextPageButton) return;

        currentPageDisplay.textContent = `Trang: ${currentPage + 1}`;
        prevPageButton.disabled = currentPage === 0;
        nextPageButton.disabled = productsOnPage.length < pSize;

        if (productsOnPage.length === 0 && currentPage > 0) {
            currentPage--;
            fetchProducts(currentPage, pageSize);
        }
    }

    function showMessage(message, type = 'info', autoDismiss = true) {
        if (!messageDisplay) return;
        messageDisplay.textContent = message;
        messageDisplay.className = 'message-area';
        messageDisplay.classList.add(`${type}-message`); // Ví dụ: 'success-message', 'error-message'
        if (type === 'info') { // Thêm style tạm cho info nếu qlsp.css không có
            messageDisplay.style.backgroundColor = '#e0e0e0';
            messageDisplay.style.color = '#333';
            messageDisplay.style.border = '1px solid #ccc';
        }
        messageDisplay.classList.remove('hidden');

        if (autoDismiss) {
            setTimeout(() => {
                messageDisplay.classList.add('hidden');
            }, 3500);
        }
    }

    if (prevPageButton) {
        prevPageButton.addEventListener('click', () => {
            if (currentPage > 0) {
                currentPage--;
                fetchProducts(currentPage, pageSize);
            }
        });
    }

    if (nextPageButton) {
        nextPageButton.addEventListener('click', () => {
            currentPage++;
            fetchProducts(currentPage, pageSize);
        });
    }

    fetchProducts(currentPage, pageSize);
});