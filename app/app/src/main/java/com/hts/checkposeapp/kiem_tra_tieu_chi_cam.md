# Kiểm tra lại cách camera check các tiêu chí

Nguồn: `viec_can_lam.md` (mục 2 trong danh sách việc cần làm).

## Vấn đề cần kiểm tra

> Cần kiểm tra lại, cách hoạt động của cam check các tiêu chí. Trường hợp khi user k hoàn thành 1 tiêu chí, thì nó sẽ next sang tiêu chí khác luôn → để user hoàn thành chụp 1 bức ảnh.

**Hiện tượng:** khi người dùng chưa đạt một tiêu chí, camera chuyển sang tiêu chí kế tiếp ngay, không giữ người dùng ở tiêu chí hiện tại cho đến khi đạt.

**Mong muốn:** người dùng phải hoàn thành các tiêu chí cần thiết rồi mới đến bước chụp ảnh.

## Các câu hỏi cần trả lời

1. **Tiêu chí lõi:** tiêu chí nào bắt buộc phải đạt trước khi chuyển sang tiêu chí khác? Tiêu chí nào chỉ là gợi ý và có thể bỏ qua?
2. **Trường hợp không nhận diện được:** nếu model không đo được một tiêu chí (ví dụ người không nằm trong khung hình), hướng dẫn có đang bị lặp vòng không? Cần tìm giải pháp để không kẹt ở đó mãi.
3. **Chuyển tiêu chí:** việc chuyển sang tiêu chí kế tiếp đang được quyết định dựa trên điều kiện nào? Có cần thêm thời gian giữ trạng thái "đạt" trước khi chuyển không?

## Hướng kiểm tra

- Đọc luồng hướng dẫn trong gói `guidance/` (đặc biệt `GuidanceEngine`, `CriterionGate`, `CuePresenter`) để xác định điều kiện chuyển tiêu chí hiện tại.
- Đối chiếu với ngưỡng trong `GuidanceConfig` và các điều kiện "lõi" đã chốt.
- Thử trên máy thật: đứng ở một tiêu chí chưa đạt và quan sát xem app có tự chuyển sang tiêu chí khác không.

## Kết quả rà soát code (21/11/2026)

Đọc tĩnh, chưa chạy trên máy thật. Các điểm dưới đây đều đã đối chiếu với code.

### 1. Câu nhắc nhảy sang tiêu chí khác ngay khi tiêu chí hiện tại chỉ mới tốt lên một chút

- **Vị trí:** `guidance/CuePresenter.kt` (nhánh `!currentStillFailing -> top`, khoảng dòng 200–218).
- **Cơ chế:** danh sách `failing` chỉ chứa mục ở trạng thái `FAILING`. Khi mục đang nhắc chuyển sang `GREY` (vùng đệm, đã bớt lệch nhưng chưa đạt), nó không còn trong `failing`, nên câu nhắc nhường chỗ **ngay**, không chờ `CUE_MIN_DISPLAY_MS`.
- **Hệ quả:** người dùng vừa đưa máy về gần đúng là câu chuyển sang tiêu chí khác. Nếu mục đó là mục duy nhất của bước hiện tại thì không còn câu nào, nên trông như app bỏ qua tiêu chí.
- **Khớp với triệu chứng trong `viec_can_lam.md`.** Đây là nguyên nhân chính của hiện tượng.
- **Cần quyết định sản phẩm:** một tiêu chí đang được nhắc có nên giữ câu cho đến khi đạt (`PASSING`), hay được phép nhường khi chỉ mới `GREY`?

### 2. Chế độ tự động bỏ qua bước theo thời gian tính từ lúc bước bắt đầu, không theo việc "kẹt"

- **Vị trí:** `guidance/GuidanceEngine.kt`, khối `allowSkip` (khoảng dòng 358–373), `currentStepSinceMs`.
- **Cơ chế:** mốc `currentStepSinceMs` được đặt khi bước bắt đầu và **không được đặt lại khi người dùng đang tiến bộ**. Chỉ xoá khi bước đổi, bước bị phá, hoặc có cảnh báo cầm máy. Sau `AUTO_SKIP_AFTER_MS` (6 giây = 3 × 2 giây) thì mọi mục chưa `PASSING` của bước bị đánh dấu `skipped` và không bao giờ được đánh giá lại.
- **Hệ quả:** trong chế độ tự động, người dùng đang điều chỉnh đúng hướng vẫn bị bỏ qua sau 6 giây kể từ lúc bước bắt đầu.
- **Lệch với ý đồ đã ghi:** `FOOTGUNS.md` mục 92 và `THAY_DOI_NHANH_THUAT_TOAN.md` đều ghi "bỏ qua nếu bước **không hội tụ** sau khoảng 6 giây". Code đo thời gian chứ không đo việc "không hội tụ".
- **Test chưa bao phủ:** `HuongDanTuanTuTest` chỉ kiểm tra hằng số `AUTO_SKIP_AFTER_MS == 6000`, không kiểm tra hành vi.

### 3. Tài liệu từng ghi các cơ chế chưa có trong code

- "Kẹt quá 20 giây thì nới dung sai, hiện 'Góc này cũng ổn, chụp thử nhé?'" và "không thấy người quá 1,5 giây thì hiện 'Đưa mẫu vào khung'": không tìm thấy trong code. Đã đánh dấu ⚠️ trong `TONG_QUAN_DU_AN.md` mục 3.8.
- Giá trị `cueMinDisplaySeconds` trong tài liệu là 1,2s, nhưng code và `FOOTGUNS.md` mục 66 đã là 2,0s. Đã sửa tài liệu.

### 4. Đã đồng bộ tài liệu

- `CLAUDE.md`, `AGENTS.md`: lệnh `adb` và đường dẫn đổi sang `com.hts.checkposeapp`. Mục "đổi định danh" đánh dấu đã xong. Thời lượng cue 2s.
- `TONG_QUAN_DU_AN.md`, `NGUONG_VA_GOC_QUY_CHIEU.md`: thời lượng cue 2,0s.

### Chưa làm

- Chưa sửa code. Hai điểm 1 và 2 đều đổi hành vi, cần chốt yêu cầu trước (xem câu hỏi ở trên).
- Chưa thêm `FOOTGUNS.md`, vì quy tắc chỉ ghi các lỗi **đã sửa**.

## Ghi chú

- Liên quan đến dòng thứ 7 trong `viec_can_lam.md` (tiêu chí lõi và chống lặp vòng hướng dẫn).
