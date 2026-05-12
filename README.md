# CrystalAI-1.21.11

## Giới thiệu

CrystalAI-1.21.11 là một mod Minecraft nâng cao được thiết kế cho phiên bản 1.21.11, cung cấp tính năng AI thông minh cho Crystal PvP. Mod này sử dụng thuật toán tiên tiến để điều khiển người chơi một cách tự động và hiệu quả trong các trận PvP.

## Tính năng chính

### Crystal PvP AI
- **Đặt Crystal thông minh**: Tự động đặt End Crystal ở vị trí tối ưu để gây sát thương cao nhất cho kẻ địch
- **Phá Crystal thông minh**: Phá crystal khi có lợi nhất về sát thương
- **Dự đoán vị trí**: Sử dụng thuật toán dự đoán để biết trước vị trí di chuyển của kẻ địch
- **Combo Crystal**: Tự động thực hiện combo crystal liên tục

### Kỹ thuật PvP nâng cao
- **W-Tap**: Tự động thực hiện kỹ thuật W-Tap để tối ưu hóa sát thương
- **S-Tap**: Tự động thực hiện kỹ thuật S-Tap để né tránh
- **Sprint Reset**: Tự động reset sprint để tối ưu hóa tốc độ di chuyển
- **Ender Pearl thông minh**: Dự đoán và sử dụng Ender Pearl để tiếp cận hoặc thoát khỏi kẻ địch

### Quản lý tự động
- **Hotbar Management**: Tự động chuyển đổi giữa End Crystal và Obsidian
- **Totem Management**: Tự động đảm bảo Totem trong tay trái (offhand)
- **Gap Apple**: Tự động ăn Golden Apple/Enchanted Golden Apple khi máu thấp
- **Obsidian Placement**: Tự động đặt Obsidian dưới chân kẻ địch

### Chế độ chiến đấu
- **Aggressive Mode**: Chế độ tấn công khi máu cao và ở gần kẻ địch
- **Tactical Mode**: Chế độ chiến thuật khi ở xa kẻ địch
- **Defensive Mode**: Chế độ phòng thủ khi máu thấp (tự động ăn Gap, dùng Totem, chạy thoát)

## Cài đặt

### Yêu cầu
- Minecraft: 1.21.11
- Fabric Loader: 0.19.2
- Fabric API: 0.141.3+1.21.11
- Java: 21+

### Cài đặt
1. Tải Fabric Loader từ [fabricmc.net](https://fabricmc.net/)
2. Cài đặt Fabric Loader vào Minecraft
3. Tải file `CrystalAI-1.21.11-1.21.11.jar`
4. Đặt file vào thư mục `.minecraft/mods/`
5. Khởi chạy Minecraft

## Sử dụng

### Bật/Tắt Mod
- **Shift Phải**: Bật hoặc tắt mod nhanh chóng
- Khi mod được bật, nó sẽ tự động hoạt động
- Khi mod bị tắt, tất cả các hành động AI sẽ dừng lại

### Tính năng Crystal PvP
- Mod sẽ tự động tìm kẻ địch gần nhất
- Tự động đặt và phá crystal
- Tự động sử dụng các kỹ thuật PvP

## Cấu hình

### Thông số mặc định
- **Khoảng cách tìm kẻ địch**: 20 blocks
- **Khoảng cách đặt crystal**: 6 blocks
- **Delay đặt crystal**: 6 ticks
- **Delay phá crystal**: 4 ticks
- **W-Tap cooldown**: 10 ticks
- **S-Tap cooldown**: 8 ticks

## Lưu ý quan trọng

- Mod chỉ dành cho Minecraft 1.21.11
- Cần Fabric Loader và Fabric API
- Mod sử dụng Yarn mappings 1.21.11+build.5
- Một số tính năng có thể cần cấu hình thêm

## Tính năng kỹ thuật

### Thuật toán AI
- Dự đoán vị trí dựa trên vận tốc của kẻ địch
- Tính toán sát thương crystal chính xác
- Quản lý cooldown tối ưu
- Hệ thống điểm số để chọn vị trí tốt nhất

### Tối ưu hóa
- Smooth rotation để tránh bị phát hiện
- Quản lý delay chính xác
- Tối ưu hóa performance

## Tác giả

- **MinhQuang**

## Giấy phép

- MIT License

## Phiên bản

- **Phiên bản hiện tại**: 1.21.11
- **Minecraft**: 1.21.11

## Hỗ trợ

Nếu gặp vấn đề, vui lòng báo cáo tại:
- GitHub Issues (nếu có repository)
- Discord server ([nếu có](https://discord.gg/xXD5J84Y))

## Lịch sử cập nhật

### Phiên bản 1.21.11 (Cập nhật mới nhất)
- Cập nhật cho Minecraft 1.21.11
- Sửa lỗi mapping cho getPos(), distanceTo(), selectedSlot
- Thêm tính năng AI Crystal PvP nâng cao (~406 dòng code)
- Thêm W-Tap, S-Tap, Sprint Reset
- Thêm quản lý hotbar và totem tự động
- Tối ưu hóa thuật toán dự đoán
- **Thêm phím Shift Phải để bật/tắt mod nhanh chóng**
- **Thêm hệ thống toggle() và isEnabled() cho CrystalAI**
- **Cập nhật keybind integration trong CrystalAIClient.java**
- Thêm ~388 dòng utility functions trong AICore.java
- Tạo file README.md chi tiết

## Đóng góp

Mod này được phát triển để học tập và nghiên cứu. Vui lòng không sử dụng vào mục đích gian lận trong các server có quy định cấm.

## Cảnh báo

- Mod này chỉ nên sử dụng trong môi trường cho phép
- Không chịu trách nhiệm nếu bị ban khỏi server
- Sử dụng có trách nhiệm
