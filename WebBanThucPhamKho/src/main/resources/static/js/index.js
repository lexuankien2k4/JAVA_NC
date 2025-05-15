const sideMenu = document.querySelector('aside');
const menuBtn = document.getElementById('menu-btn'); // Sửa typo 'nenuBtn' thành 'menuBtn'
const closeBtn = document.getElementById('close-btn');
const darkMode = document.querySelector('.dark-mode');
const sidebarLinks = document.querySelectorAll('.sidebar a');
const mainContent = document.querySelector('main'); // Lấy phần main để thay đổi nội dung

// Hiển thị sidebar khi click vào nút menu
menuBtn.addEventListener('click', () => {
    sideMenu.style.display = 'block';
});

// Ẩn sidebar khi click vào nút close
closeBtn.addEventListener('click', () => {
    sideMenu.style.display = 'none';
});

// Chuyển đổi chế độ tối khi click vào biểu tượng chế độ tối
darkMode.addEventListener('click', () => {
    document.body.classList.toggle('dark-mode-variables');
    darkMode.querySelector('span:nth-child(1)').classList.toggle('active');
    darkMode.querySelector('span:nth-child(2)').classList.toggle('active');
});

// Hàm loại bỏ lớp 'active' khỏi tất cả các mục trong sidebar
function removeActiveClass() {
    sidebarLinks.forEach(link => link.classList.remove('active'));
}

// Lấy tất cả các phần tử content
const contentSections = document.querySelectorAll('.content');

// Hàm loại bỏ lớp 'active' khỏi tất cả các mục và ẩn các content
function removeActiveClass() {
    sidebarLinks.forEach(link => link.classList.remove('active')); // Xóa 'active' khỏi sidebar
    contentSections.forEach(content => content.classList.remove('active')); // Ẩn tất cả các content
}

// Thêm sự kiện click vào từng mục trong sidebar
sidebarLinks.forEach((link) => {
    link.addEventListener('click', function () {
        removeActiveClass(); // Xóa các lớp active hiện tại
        this.classList.add('active'); // Thêm lớp active vào sidebar được click


    });
});