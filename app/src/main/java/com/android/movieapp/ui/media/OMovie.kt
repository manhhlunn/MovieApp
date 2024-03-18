package com.android.movieapp.ui.media



enum class OMovieType(val value: String, val displayName: String) {
    PhimMoi("phim-moi", "Phim mới"),
    PhimBo("phim-bo", "Phim bộ"),
    PhimLe("phim-le", "Phim lẻ"),
    TvShows("tv-shows", "TV shows"),
    HoatHinh("hoat-hinh", "Hoạt hình"),
    ThuyetMinh("phim-thuyet-minh", "Phim thuyết minh"),
    LongTieng("phim-long-tieng", "Phim lồng tiếng"),
    PhimBoDangChieu("phim-bo-dang-chieu", "Phim bộ đang chiếu"),
    PhimBoHoanThanh("phim-bo-hoan-thanh", "Phim bộ hoàn thành"),
    PhimSapChieu("phim-sap-chieu", "Phim sắp chiếu"),
    SubTeam("subteam", "Subteam");

    fun getFile(): String {
        return "/danh-sach/$value.json"
    }

    fun getSlug(): String {
        return "/danh-sach/$value"
    }
}

enum class FilterCategory(val value: String, val displayName: String) {
    HanhDong("hanh-dong", "Hành động"),
    TinhCam("tinh-cam", "Tình cảm"),
    HaiHuoc("hai-huoc", "Hài hước"),
    CoTrang("co-trang", "Cổ trang"),
    TamLy("tam-ly", "Tâm lý"),
    HinhSu("hinh-su", "Hình sự"),
    ChienTranh("chien-tranh", "Chiến tranh"),
    TheThao("the-thao", "Thể thao"),
    VoThuat("vo-thuat", "Võ thuật"),
    VienTuong("vien-tuong", "Viễn tưởng"),
    PhieuLuu("phieu-luu", "Phiêu lưu"),
    KhoaHoc("khoa-hoc", "Khoa học"),
    KinhDi("kinh-di", "Kinh dị"),
    AmNhac("am-nhac", "Âm nhạc"),
    ThanThoai("than-thoai", "Thần thoại"),
    TaiLieu("tai-lieu", "Tài liệu"),
    GiaDinh("gia-dinh", "Gia đình"),
    ChinhKich("chinh-kich", "Chính kịch"),
    BiAn("bi-an", "Bí ẩn"),
    HocDuong("hoc-duong", "Học đường"),
    KinhDien("kinh-dien", "Kinh điển"),
    X("phim-18", "18+");

}

enum class FilterCountry(val value: String, val displayName: String) {
    TrungQuoc("trung-quoc", "Trung Quốc"),
    HanQuoc("han-quoc", "Hàn Quốc"),
    NhatBan("nhat-ban", "Nhật Bản"),
    ThaiLan("thai-lan", "Thái Lan"),
    AuMy("au-my", "Âu Mỹ"),
    DaiLoan("dai-loan", "Đài Loan"),
    HongKong("hong-kong", "Hồng Kông"),
    AnDo("an-do", "Ấn Độ"),
    Anh("anh", "Anh"),
    Phap("phap", "Pháp"),
    Canada("canada", "Canada"),
    Other("other", "Khác"),
    Duc("duc", "Đức"),
    TayBanNha("tay-ban-nha", "Tây Ban Nha"),
    ThoNhiKy("tho-nhi-ky", "Thổ Nhĩ Kỳ"),
    HaLan("ha-lan", "Hà Lan"),
    Indonesia("indonesia", "Indonesia"),
    Nga("nga", "Nga"),
    Mexico("mexico", "Mexico"),
    BaLan("ba-lan", "Ba Lan"),
    Uc("uc", "Úc"),
    ThuyDien("thuy-dien", "Thụy Điển"),
    Malaysia("malaysia", "Malaysia"),
    Brazil("brazil", "Brazil"),
    Philippines("philippines", "Philippines"),
    BoDaoNha("bo-dao-nha", "Bồ Đào Nha"),
    Y("y", "Ý"),
    DanMach("dan-mach", "Đan Mạnh"),
    UAE("uae", "UAE"),
    NaUy("na-uy", "Na Uy"),
    ThuySy("thuy-sy", "Thụy Sỹ"),
    ChauPhi("chau-phi", "Châu Phi"),
    NamPhi("nam-phi", "Nam Phi"),
    Ukraina("ukraina", "Ukraina"),
    ARapXeUt("a-rap-xe-ut", "Ả rập xê út");
}

