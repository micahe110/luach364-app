package org.luach364.app;

import java.time.LocalDate;

/**
 * כל הטקסטים בשבע שפות: עברית, רוסית, אנגלית, גרמנית, צרפתית, סינית, יפנית.
 * כדי לתקן תרגום — משנים כאן בלבד.
 */
public final class I18n {
    private I18n() {}

    public static final String[] CODES = {"he", "ru", "en", "de", "fr", "zh", "ja"};
    public static final String[] NAMES = {"עברית", "Русский", "English", "Deutsch", "Français", "中文", "日本語"};

    public static final class L {
        public String code; public boolean rtl;
        public String[] bibWeekday, gregWeekday, gregMonth, monthOrd, course, feast, count, city;
        // תבניות: {d} יום, {m} חודש, {n} מספר, {y} שנה, {k} יום בחג, {a}/{b} משמרות
        public String title, gregFmt, yearFmt, signGamul, signShecaniah, courseOne, courseSwap,
                icTitle, icSub, countFmt, shmita, info, moonX, moonD, moonIn, beforeSunrise,
                sunrise, sunset, partFmt, kindDay, kindNight, gate, dayK;
        // ממשק האפליקציה
        public String uiTitle, uiHint, uiSet, uiPick, uiNoBg, uiDial, uiDark, uiHome, uiHeight,
                uiLoc, uiSaveLoc, uiSaved, uiBadLoc, uiBadImg, uiLang;
    }

    public static L get(String code) {
        switch (code == null ? "he" : code) {
            case "ru": return ru();
            case "en": return en();
            case "de": return de();
            case "fr": return fr();
            case "zh": return zh();
            case "ja": return ja();
            default: return he();
        }
    }

    // ===================== עזרים =====================
    static String num(L l, int n) { return l.code.equals("he") ? Luach.gem(n) : String.valueOf(n); }

    static String zhNum(int n) {
        String[] d = {"", "一", "二", "三", "四", "五", "六", "七", "八", "九"};
        if (n < 10) return d[n];
        if (n == 10) return "十";
        if (n < 20) return "十" + d[n % 10];
        return d[n / 10] + "十" + d[n % 10];
    }

    static String enOrd(int n) {
        int m = n % 100;
        if (m >= 11 && m <= 13) return n + "th";
        switch (n % 10) { case 1: return n + "st"; case 2: return n + "nd"; case 3: return n + "rd"; default: return n + "th"; }
    }

    /** תאריך מקראי ראשי, למשל "כ״ה לחודש השביעי" / "25th day of the seventh month". */
    public static String title(L l, Luach.Z z) {
        if (z.ic) return l.icTitle.replace("{n}", num(l, z.icDay));
        String d;
        switch (l.code) {
            case "en": d = enOrd(z.day); break;
            case "fr": d = z.day == 1 ? "1er" : z.day + "e"; break;
            case "zh": d = zhNum(z.day); break;
            case "he": d = Luach.gem(z.day); break;
            default: d = String.valueOf(z.day);
        }
        String m = l.code.equals("zh") ? zhNum(z.month) : l.monthOrd[z.month - 1];
        return l.title.replace("{d}", d).replace("{m}", m);
    }

    public static String gregLine(L l, LocalDate g) {
        int wd = g.getDayOfWeek().getValue() % 7;
        return l.gregFmt.replace("{w}", l.gregWeekday[wd]).replace("{d}", String.valueOf(g.getDayOfMonth()))
                .replace("{m}", l.gregMonth[g.getMonthValue() - 1]).replace("{mn}", String.valueOf(g.getMonthValue()))
                .replace("{y}", String.valueOf(g.getYear()));
    }

    public static String subLine(L l, Luach.Z z) {
        if (z.ic) return l.bibWeekday[z.wd] + " · " + l.icSub;
        String s = l.bibWeekday[z.wd] + " · " + l.yearFmt.replace("{n}", num(l, z.year));
        if (z.year == 1) s += " · " + l.signGamul;
        if (z.year == 4) s += " · " + l.signShecaniah;
        return s;
    }

    public static String courseLine(L l, Luach.Z z) {
        int in = idx(z.cin), out = idx(z.cout);
        if (z.wd == 6 && in != out) return l.courseSwap.replace("{a}", l.course[out]).replace("{b}", l.course[in]);
        return l.courseOne.replace("{a}", l.course[in]);
    }

    static int idx(String heCourse) {
        for (int i = 0; i < Luach.COURSES.length; i++) if (Luach.COURSES[i].equals(heCourse)) return i;
        return 21;
    }

    /** שם המועד בשפה, או null. */
    public static String feast(L l, Luach.Z z) {
        if (z.ic) return null;
        int m = z.month, d = z.day, i = -1, k = 0;
        if (m == 1 && d == 1) i = 0;
        else if (m == 1 && d == 14) i = 1;
        else if (m == 1 && d >= 15 && d <= 21) { i = 2; k = d - 14; }
        else if (m == 1 && d == 26) i = 3;
        else if (m == 2 && d == 14) i = 4;
        else if (m == 3 && d == 15) i = 5;
        else if (m == 4 && d == 1) i = 6;
        else if (m == 5 && d == 3) i = 7;
        else if (m == 6 && d == 22) i = 8;
        else if (m == 6 && d >= 23 && d <= 28) { i = 9; k = d - 22; }
        else if (m == 7 && d == 1) i = 10;
        else if (m == 7 && d == 10) i = 11;
        else if (m == 7 && d >= 15 && d <= 21) { i = 12; k = d - 14; }
        else if (m == 7 && d == 22) i = 13;
        else if (m == 10 && d == 1) i = 14;
        if (i < 0) return null;
        String s = l.feast[i];
        return k > 0 ? s.replace("{k}", num(l, k)) : s;
    }

    public static String countLine(L l, Luach.Z z) {
        if (z.countLabel == null) return null;
        int c = z.countLabel.equals(Luach.COUNT_LABEL[0]) ? 0 : (z.countLabel.equals(Luach.COUNT_LABEL[1]) ? 1 : 2);
        return l.countFmt.replace("{n}", String.valueOf(z.countN)).replace("{c}", l.count[c]);
    }

    public static String infoLine(L l, Luach.State s) {
        Luach.Z z = s.z;
        String moon = z.isX ? l.moonX : (z.isD ? l.moonD : l.moonIn.replace("{n}", String.valueOf(z.toX)));
        return l.info.replace("{g}", String.valueOf(s.gate)).replace("{d}", String.valueOf(s.dp))
                .replace("{n}", String.valueOf(s.np)) + " · " + moon;
    }

    public static String sunLine(L l, Luach.State s) {
        return l.sunrise + " " + Luach.hhmm(s.rise) + "   •   " + l.sunset + " " + Luach.hhmm(s.set);
    }

    public static String partLine(L l, Luach.State s) {
        return l.partFmt.replace("{n}", String.valueOf(s.part)).replace("{m}", String.valueOf(s.of));
    }

    // ===================== עברית =====================
    static L he() {
        L l = new L(); l.code = "he"; l.rtl = true;
        l.bibWeekday = Luach.WEEKDAYS;
        l.gregWeekday = new String[]{"יום ראשון","יום שני","יום שלישי","יום רביעי","יום חמישי","יום שישי","שבת"};
        l.gregMonth = Luach.GREG;
        l.monthOrd = Luach.MONTH_ORD;
        l.course = Luach.COURSES;
        l.feast = new String[]{"ראש השנה · יום זיכרון","פסח","חג המצות — {k}","הנפת העומר · ראשית הקציר","פסח שני",
                "חג השבועות · ביכורי חיטים","יום זיכרון · ראש הקיץ","מועד היין · ביכורי תירוש","מועד השמן · ביכורי יצהר",
                "קרבן העצים — {k}","יום תרועה · זיכרון תרועה","יום הכיפורים","חג הסוכות — {k}","שמיני עצרת · יום ההקהל",
                "יום זיכרון · ראש החורף"};
        l.count = new String[]{"לעומר","לספירת התירוש","לספירת היצהר"};
        l.city = new String[]{"אשקלון","ירושלים","תל אביב","חיפה","באר שבע","אילת","מותאם אישית"};
        l.title = "{d} לחודש {m}"; l.gregFmt = "{w} {d} ב{m} {y}"; l.yearFmt = "שנת {n} במחזור";
        l.signGamul = "אות גמול"; l.signShecaniah = "אות שכניה";
        l.courseOne = "משמרת {a}"; l.courseSwap = "משמרת {a} יוצאת · {b} נכנסת";
        l.icTitle = "שבוע העיבור · יום {n}"; l.icSub = "כל הכהנים עם גמול";
        l.countFmt = "היום {n} {c}"; l.shmita = "שנת השמיטה";
        l.info = "שער {g} · יום {d} חלקים · לילה {n}";
        l.moonX = "X — מילוא סכמטי"; l.moonD = "דוקה — ליל הסהר הראשון"; l.moonIn = "X בעוד {n} ימים";
        l.beforeSunrise = "לפני הזריחה — עדיין היום המקראי של אתמול";
        l.sunrise = "זריחה"; l.sunset = "שקיעה";
        l.partFmt = "חלק {n} מתוך {m}"; l.kindDay = "היום"; l.kindNight = "הלילה"; l.gate = "שער {n}";
        l.uiTitle = "לוח 364 · שעון חנוך";
        l.uiHint = "רקע חי: במסך הנעילה — שעון חנוך בזמן אמת והתאריך המקראי. במסך הבית — רק התמונה שלך.";
        l.uiSet = "הגדר כרקע (לבחור: מסך הבית ומסך הנעילה)"; l.uiPick = "בחר תמונת רקע"; l.uiNoBg = "בלי תמונה (רקע ברירת מחדל)";
        l.uiDial = "להציג את שעון חנוך"; l.uiDark = "עיצוב כהה (במקום הבהיר)"; l.uiHome = "להציג את השעון והתאריך גם במסך הבית";
        l.uiHeight = "גובה התוכן במסך (כדי לא להתנגש בשעון של סמסונג)"; l.uiLoc = "מיקום לחישוב זריחה ושקיעה";
        l.uiSaveLoc = "שמור מיקום"; l.uiSaved = "נשמר"; l.uiBadLoc = "קו רוחב/אורך לא תקין";
        l.uiBadImg = "לא הצלחתי לקרוא את התמונה"; l.uiLang = "שפה";
        return l;
    }

    // ===================== רוסית =====================
    static L ru() {
        L l = new L(); l.code = "ru"; l.rtl = false;
        l.bibWeekday = new String[]{"Первый день","Второй день","Третий день","Четвёртый день","Пятый день","Шестой день","Суббота"};
        l.gregWeekday = new String[]{"Воскресенье","Понедельник","Вторник","Среда","Четверг","Пятница","Суббота"};
        l.gregMonth = new String[]{"января","февраля","марта","апреля","мая","июня","июля","августа","сентября","октября","ноября","декабря"};
        l.monthOrd = new String[]{"первого","второго","третьего","четвёртого","пятого","шестого","седьмого","восьмого",
                "девятого","десятого","одиннадцатого","двенадцатого"};
        l.course = new String[]{"Иехоиарив","Иедаия","Харим","Сеорим","Малхия","Миямин","Гаккоц","Авия","Иешуа","Шехания",
                "Елиашив","Иаким","Хуппа","Иешевав","Билга","Иммер","Хезир","Гаппиццец","Петахия","Иезекииль","Иахин","Гамул",
                "Делаия","Маазия"};
        l.feast = new String[]{"Новый год · День памяти","Песах","Праздник опресноков — день {k}","Возношение снопа · Начало жатвы",
                "Второй Песах","Шавуот · Первые плоды пшеницы","День памяти · начало лета","Праздник вина · Первые плоды винограда",
                "Праздник масла · Первые плоды олив","Приношение дров — день {k}","День трубного гласа","День искупления",
                "Праздник Кущей — день {k}","Восьмой день собрания","День памяти · начало зимы"};
        l.count = new String[]{"счёта омера","счёта вина","счёта масла"};
        l.city = new String[]{"Ашкелон","Иерусалим","Тель-Авив","Хайфа","Беэр-Шева","Эйлат","Свои координаты"};
        l.title = "{d}-й день {m} месяца"; l.gregFmt = "{w}, {d} {m} {y}"; l.yearFmt = "год {n} цикла";
        l.signGamul = "знамение Гамул"; l.signShecaniah = "знамение Шехания";
        l.courseOne = "Череда {a}"; l.courseSwap = "Череда {a} уходит · {b} приходит";
        l.icTitle = "Добавочная неделя · день {n}"; l.icSub = "все священники с Гамулом";
        l.countFmt = "День {n} {c}"; l.shmita = "Субботний год (шмита)";
        l.info = "Врата {g} · день {d} частей · ночь {n}";
        l.moonX = "X — схематическое полнолуние"; l.moonD = "Дука — ночь первого серпа"; l.moonIn = "X через {n} дн.";
        l.beforeSunrise = "До восхода — ещё вчерашний библейский день";
        l.sunrise = "Восход"; l.sunset = "Закат";
        l.partFmt = "часть {n} из {m}"; l.kindDay = "день"; l.kindNight = "ночь"; l.gate = "врата {n}";
        l.uiTitle = "Календарь 364 · часы Еноха";
        l.uiHint = "Живые обои: на экране блокировки — часы Еноха в реальном времени и библейская дата. На главном экране — только ваша картинка.";
        l.uiSet = "Установить обои (выбрать: главный экран и экран блокировки)"; l.uiPick = "Выбрать фоновое изображение";
        l.uiNoBg = "Без изображения (фон по умолчанию)"; l.uiDial = "Показывать часы Еноха"; l.uiDark = "Тёмное оформление";
        l.uiHome = "Показывать часы и дату и на главном экране";
        l.uiHeight = "Высота содержимого (чтобы не перекрывать часы Samsung)"; l.uiLoc = "Место для расчёта восхода и заката";
        l.uiSaveLoc = "Сохранить место"; l.uiSaved = "Сохранено"; l.uiBadLoc = "Неверные координаты";
        l.uiBadImg = "Не удалось прочитать изображение"; l.uiLang = "Язык";
        return l;
    }

    // ===================== אנגלית =====================
    static L en() {
        L l = new L(); l.code = "en"; l.rtl = false;
        l.bibWeekday = new String[]{"First day","Second day","Third day","Fourth day","Fifth day","Sixth day","Sabbath"};
        l.gregWeekday = new String[]{"Sunday","Monday","Tuesday","Wednesday","Thursday","Friday","Saturday"};
        l.gregMonth = new String[]{"January","February","March","April","May","June","July","August","September","October","November","December"};
        l.monthOrd = new String[]{"first","second","third","fourth","fifth","sixth","seventh","eighth","ninth","tenth","eleventh","twelfth"};
        l.course = new String[]{"Jehoiarib","Jedaiah","Harim","Seorim","Malchijah","Mijamin","Hakkoz","Abijah","Jeshua","Shecaniah",
                "Eliashib","Jakim","Huppah","Jeshebeab","Bilgah","Immer","Hezir","Happizzez","Pethahiah","Jehezkel","Jachin","Gamul",
                "Delaiah","Maaziah"};
        l.feast = new String[]{"New Year · Day of Remembrance","Passover","Feast of Unleavened Bread — day {k}",
                "Wave Sheaf · Start of Harvest","Second Passover","Feast of Weeks · Firstfruits of Wheat",
                "Day of Remembrance · Start of Summer","Feast of New Wine","Feast of New Oil","Wood Offering — day {k}",
                "Day of Trumpets","Day of Atonement","Feast of Tabernacles — day {k}","Eighth Day of Assembly",
                "Day of Remembrance · Start of Winter"};
        l.count = new String[]{"of the Omer count","of the wine count","of the oil count"};
        l.city = new String[]{"Ashkelon","Jerusalem","Tel Aviv","Haifa","Beersheba","Eilat","Custom"};
        l.title = "{d} day of the {m} month"; l.gregFmt = "{w}, {d} {m} {y}"; l.yearFmt = "year {n} of the cycle";
        l.signGamul = "sign of Gamul"; l.signShecaniah = "sign of Shecaniah";
        l.courseOne = "Course of {a}"; l.courseSwap = "{a} departs · {b} enters";
        l.icTitle = "Intercalary week · day {n}"; l.icSub = "all priests with Gamul";
        l.countFmt = "Day {n} {c}"; l.shmita = "Sabbatical year (Shmita)";
        l.info = "Gate {g} · day {d} parts · night {n}";
        l.moonX = "X — schematic full moon"; l.moonD = "Duqah — night of the first crescent"; l.moonIn = "X in {n} days";
        l.beforeSunrise = "Before sunrise — still yesterday's biblical day";
        l.sunrise = "Sunrise"; l.sunset = "Sunset";
        l.partFmt = "part {n} of {m}"; l.kindDay = "day"; l.kindNight = "night"; l.gate = "gate {n}";
        l.uiTitle = "Calendar 364 · Enoch Clock";
        l.uiHint = "Live wallpaper: the lock screen shows the Enoch clock in real time and the biblical date. The home screen shows only your picture.";
        l.uiSet = "Set as wallpaper (choose: home and lock screens)"; l.uiPick = "Choose background image";
        l.uiNoBg = "No image (default background)"; l.uiDial = "Show the Enoch clock"; l.uiDark = "Dark theme";
        l.uiHome = "Also show clock and date on the home screen";
        l.uiHeight = "Content height (to avoid the Samsung clock)"; l.uiLoc = "Location for sunrise and sunset";
        l.uiSaveLoc = "Save location"; l.uiSaved = "Saved"; l.uiBadLoc = "Invalid coordinates";
        l.uiBadImg = "Could not read the image"; l.uiLang = "Language";
        return l;
    }

    // ===================== גרמנית =====================
    static L de() {
        L l = new L(); l.code = "de"; l.rtl = false;
        l.bibWeekday = new String[]{"Erster Tag","Zweiter Tag","Dritter Tag","Vierter Tag","Fünfter Tag","Sechster Tag","Sabbat"};
        l.gregWeekday = new String[]{"Sonntag","Montag","Dienstag","Mittwoch","Donnerstag","Freitag","Samstag"};
        l.gregMonth = new String[]{"Januar","Februar","März","April","Mai","Juni","Juli","August","September","Oktober","November","Dezember"};
        l.monthOrd = new String[]{"ersten","zweiten","dritten","vierten","fünften","sechsten","siebten","achten","neunten",
                "zehnten","elften","zwölften"};
        l.course = new String[]{"Jojarib","Jedaja","Harim","Seorim","Malkija","Mijamin","Hakkoz","Abija","Jeschua","Schechanja",
                "Eljaschib","Jakim","Huppa","Jeschebeab","Bilga","Immer","Hesir","Happizzez","Petachja","Jeheskel","Jachin","Gamul",
                "Delaja","Maasja"};
        l.feast = new String[]{"Neujahr · Tag des Gedenkens","Passah","Fest der ungesäuerten Brote — Tag {k}",
                "Webegarbe · Beginn der Ernte","Zweites Passah","Wochenfest · Erstlinge des Weizens",
                "Tag des Gedenkens · Sommeranfang","Fest des neuen Weins","Fest des neuen Öls","Holzopfer — Tag {k}",
                "Tag des Posaunenschalls","Versöhnungstag","Laubhüttenfest — Tag {k}","Achter Tag der Versammlung",
                "Tag des Gedenkens · Winteranfang"};
        l.count = new String[]{"der Omer-Zählung","der Wein-Zählung","der Öl-Zählung"};
        l.city = new String[]{"Aschkelon","Jerusalem","Tel Aviv","Haifa","Beerscheba","Eilat","Eigene Koordinaten"};
        l.title = "{d}. Tag des {m} Monats"; l.gregFmt = "{w}, {d}. {m} {y}"; l.yearFmt = "Jahr {n} des Zyklus";
        l.signGamul = "Zeichen Gamul"; l.signShecaniah = "Zeichen Schechanja";
        l.courseOne = "Dienstabteilung {a}"; l.courseSwap = "{a} geht · {b} kommt";
        l.icTitle = "Schaltwoche · Tag {n}"; l.icSub = "alle Priester mit Gamul";
        l.countFmt = "Tag {n} {c}"; l.shmita = "Sabbatjahr (Schmitta)";
        l.info = "Tor {g} · Tag {d} Teile · Nacht {n}";
        l.moonX = "X — schematischer Vollmond"; l.moonD = "Duqa — Nacht der ersten Sichel"; l.moonIn = "X in {n} Tagen";
        l.beforeSunrise = "Vor Sonnenaufgang — noch der gestrige biblische Tag";
        l.sunrise = "Aufgang"; l.sunset = "Untergang";
        l.partFmt = "Teil {n} von {m}"; l.kindDay = "Tag"; l.kindNight = "Nacht"; l.gate = "Tor {n}";
        l.uiTitle = "Kalender 364 · Henoch-Uhr";
        l.uiHint = "Live-Hintergrund: Sperrbildschirm mit Henoch-Uhr in Echtzeit und biblischem Datum. Startbildschirm nur mit Ihrem Bild.";
        l.uiSet = "Als Hintergrund festlegen (wählen: Start- und Sperrbildschirm)"; l.uiPick = "Hintergrundbild wählen";
        l.uiNoBg = "Kein Bild (Standardhintergrund)"; l.uiDial = "Henoch-Uhr anzeigen"; l.uiDark = "Dunkles Design";
        l.uiHome = "Uhr und Datum auch auf dem Startbildschirm";
        l.uiHeight = "Höhe des Inhalts (damit die Samsung-Uhr frei bleibt)"; l.uiLoc = "Ort für Sonnenauf- und -untergang";
        l.uiSaveLoc = "Ort speichern"; l.uiSaved = "Gespeichert"; l.uiBadLoc = "Ungültige Koordinaten";
        l.uiBadImg = "Bild konnte nicht gelesen werden"; l.uiLang = "Sprache";
        return l;
    }

    // ===================== צרפתית =====================
    static L fr() {
        L l = new L(); l.code = "fr"; l.rtl = false;
        l.bibWeekday = new String[]{"Premier jour","Deuxième jour","Troisième jour","Quatrième jour","Cinquième jour","Sixième jour","Sabbat"};
        l.gregWeekday = new String[]{"dimanche","lundi","mardi","mercredi","jeudi","vendredi","samedi"};
        l.gregMonth = new String[]{"janvier","février","mars","avril","mai","juin","juillet","août","septembre","octobre","novembre","décembre"};
        l.monthOrd = new String[]{"premier","deuxième","troisième","quatrième","cinquième","sixième","septième","huitième",
                "neuvième","dixième","onzième","douzième"};
        l.course = new String[]{"Jehojarib","Jedaeja","Harim","Seorim","Malkija","Mijamin","Hakkots","Abija","Josué","Schecanja",
                "Éliaschib","Jakim","Huppa","Jeschébeab","Bilga","Immer","Hézir","Happitsets","Pethachja","Ézéchiel","Jakin","Gamul",
                "Delaja","Maazia"};
        l.feast = new String[]{"Nouvel An · Jour du souvenir","Pâque","Fête des pains sans levain — jour {k}",
                "Gerbe agitée · Début de la moisson","Seconde Pâque","Fête des semaines · Prémices du blé",
                "Jour du souvenir · Début de l'été","Fête du vin nouveau","Fête de l'huile nouvelle","Offrande du bois — jour {k}",
                "Jour des trompettes","Jour des expiations","Fête des tabernacles — jour {k}","Huitième jour d'assemblée",
                "Jour du souvenir · Début de l'hiver"};
        l.count = new String[]{"du compte de l'omer","du compte du vin","du compte de l'huile"};
        l.city = new String[]{"Ashkelon","Jérusalem","Tel Aviv","Haïfa","Beer-Sheva","Eilat","Coordonnées personnalisées"};
        l.title = "{d} jour du {m} mois"; l.gregFmt = "{w} {d} {m} {y}"; l.yearFmt = "année {n} du cycle";
        l.signGamul = "signe de Gamul"; l.signShecaniah = "signe de Schecanja";
        l.courseOne = "Classe de {a}"; l.courseSwap = "{a} sort · {b} entre";
        l.icTitle = "Semaine intercalaire · jour {n}"; l.icSub = "tous les prêtres avec Gamul";
        l.countFmt = "Jour {n} {c}"; l.shmita = "Année sabbatique (Shmita)";
        l.info = "Porte {g} · jour {d} parts · nuit {n}";
        l.moonX = "X — pleine lune schématique"; l.moonD = "Douqa — nuit du premier croissant"; l.moonIn = "X dans {n} jours";
        l.beforeSunrise = "Avant le lever du soleil — encore le jour biblique d'hier";
        l.sunrise = "Lever"; l.sunset = "Coucher";
        l.partFmt = "part {n} sur {m}"; l.kindDay = "jour"; l.kindNight = "nuit"; l.gate = "porte {n}";
        l.uiTitle = "Calendrier 364 · Horloge d'Hénoch";
        l.uiHint = "Fond d'écran animé : l'écran de verrouillage affiche l'horloge d'Hénoch en temps réel et la date biblique. L'écran d'accueil n'affiche que votre image.";
        l.uiSet = "Définir comme fond d'écran (choisir : accueil et verrouillage)"; l.uiPick = "Choisir l'image de fond";
        l.uiNoBg = "Sans image (fond par défaut)"; l.uiDial = "Afficher l'horloge d'Hénoch"; l.uiDark = "Thème sombre";
        l.uiHome = "Afficher aussi l'horloge et la date sur l'écran d'accueil";
        l.uiHeight = "Hauteur du contenu (pour ne pas couvrir l'horloge Samsung)"; l.uiLoc = "Lieu pour le lever et le coucher du soleil";
        l.uiSaveLoc = "Enregistrer le lieu"; l.uiSaved = "Enregistré"; l.uiBadLoc = "Coordonnées invalides";
        l.uiBadImg = "Impossible de lire l'image"; l.uiLang = "Langue";
        return l;
    }

    // ===================== סינית =====================
    static L zh() {
        L l = new L(); l.code = "zh"; l.rtl = false;
        l.bibWeekday = new String[]{"第一日","第二日","第三日","第四日","第五日","第六日","安息日"};
        l.gregWeekday = new String[]{"星期日","星期一","星期二","星期三","星期四","星期五","星期六"};
        l.gregMonth = new String[]{"1","2","3","4","5","6","7","8","9","10","11","12"};
        l.monthOrd = new String[12];
        l.course = new String[]{"耶何雅立","耶大雅","哈琳","梭琳","玛基雅","米雅民","哈歌斯","亚比雅","耶书亚","示迦尼",
                "以利亚实","雅金","胡巴","耶示比押","璧迦","音麦","希悉","哈辟悉","毗他希雅","以西结","雅斤","迦末","第来雅","玛西亚"};
        l.feast = new String[]{"新年 · 纪念日","逾越节","除酵节 — 第{k}日","摇禾捆 · 收割开始","第二逾越节","七七节 · 初熟麦子",
                "纪念日 · 夏季开始","新酒节","新油节","献柴 — 第{k}日","吹角节","赎罪日","住棚节 — 第{k}日","第八日严肃会",
                "纪念日 · 冬季开始"};
        l.count = new String[]{"数算禾捆","数算新酒","数算新油"};
        l.city = new String[]{"亚实基伦","耶路撒冷","特拉维夫","海法","别是巴","埃拉特","自定义"};
        l.title = "{m}月{d}日"; l.gregFmt = "{y}年{mn}月{d}日 {w}"; l.yearFmt = "周期第{n}年";
        l.signGamul = "迦末之兆"; l.signShecaniah = "示迦尼之兆";
        l.courseOne = "{a}班"; l.courseSwap = "{a}班退班 · {b}班进班";
        l.icTitle = "闰周 · 第{n}日"; l.icSub = "众祭司与迦末同班";
        l.countFmt = "{c}第{n}日"; l.shmita = "安息年";
        l.info = "第{g}门 · 昼{d}份 · 夜{n}份";
        l.moonX = "X — 推算满月"; l.moonD = "杜卡 — 初月之夜"; l.moonIn = "距X还有{n}日";
        l.beforeSunrise = "日出之前 — 仍属昨日的圣经日";
        l.sunrise = "日出"; l.sunset = "日落";
        l.partFmt = "第{n}份 · 共{m}份"; l.kindDay = "昼"; l.kindNight = "夜"; l.gate = "第{n}门";
        l.uiTitle = "364日历 · 以诺时钟";
        l.uiHint = "动态壁纸：锁屏显示实时以诺时钟和圣经日期；主屏幕只显示您的图片。";
        l.uiSet = "设为壁纸（选择：主屏幕和锁屏）"; l.uiPick = "选择背景图片"; l.uiNoBg = "不用图片（默认背景）";
        l.uiDial = "显示以诺时钟"; l.uiDark = "深色主题"; l.uiHome = "主屏幕也显示时钟和日期";
        l.uiHeight = "内容高度（避免遮挡三星时钟）"; l.uiLoc = "用于计算日出日落的地点";
        l.uiSaveLoc = "保存地点"; l.uiSaved = "已保存"; l.uiBadLoc = "坐标无效"; l.uiBadImg = "无法读取图片"; l.uiLang = "语言";
        return l;
    }

    // ===================== יפנית =====================
    static L ja() {
        L l = new L(); l.code = "ja"; l.rtl = false;
        l.bibWeekday = new String[]{"第一日","第二日","第三日","第四日","第五日","第六日","安息日"};
        l.gregWeekday = new String[]{"日曜日","月曜日","火曜日","水曜日","木曜日","金曜日","土曜日"};
        l.gregMonth = new String[]{"1","2","3","4","5","6","7","8","9","10","11","12"};
        l.monthOrd = new String[]{"第一","第二","第三","第四","第五","第六","第七","第八","第九","第十","第十一","第十二"};
        l.course = new String[]{"ヨヤリブ","エダヤ","ハリム","セオリム","マルキヤ","ミヤミン","ハッコツ","アビヤ","エシュア","シカニヤ",
                "エリアシブ","ヤキム","フッパ","エシェバブ","ビルガ","インメル","ヘジル","ハピツェツ","ペタヒヤ","エヘズケル","ヤキン",
                "ガムル","デラヤ","マアズヤ"};
        l.feast = new String[]{"新年 · 記念の日","過越祭","除酵祭 — 第{k}日","揺祭の束 · 刈り入れの始め","第二の過越",
                "七週祭 · 小麦の初穂","記念の日 · 夏の始め","新しいぶどう酒の祭","新しい油の祭","薪の献げ物 — 第{k}日",
                "ラッパの日","贖罪日","仮庵祭 — 第{k}日","第八日の聖会","記念の日 · 冬の始め"};
        l.count = new String[]{"オメルの数え","ぶどう酒の数え","油の数え"};
        l.city = new String[]{"アシュケロン","エルサレム","テルアビブ","ハイファ","ベエルシェバ","エイラート","カスタム"};
        l.title = "{m}の月 {d}日"; l.gregFmt = "{y}年{mn}月{d}日 {w}"; l.yearFmt = "周期の第{n}年";
        l.signGamul = "ガムルのしるし"; l.signShecaniah = "シカニヤのしるし";
        l.courseOne = "{a}組"; l.courseSwap = "{a}組 退出 · {b}組 入る";
        l.icTitle = "閏週 · 第{n}日"; l.icSub = "全祭司がガムルと共に";
        l.countFmt = "{c} 第{n}日"; l.shmita = "安息年（シュミッター）";
        l.info = "第{g}門 · 昼{d} · 夜{n}";
        l.moonX = "X — 計算上の満月"; l.moonD = "ドゥカ — 最初の三日月の夜"; l.moonIn = "Xまであと{n}日";
        l.beforeSunrise = "日の出前 — まだ昨日の聖書の日";
        l.sunrise = "日の出"; l.sunset = "日の入り";
        l.partFmt = "第{n}部 / {m}"; l.kindDay = "昼"; l.kindNight = "夜"; l.gate = "第{n}門";
        l.uiTitle = "364暦 · エノクの時計";
        l.uiHint = "ライブ壁紙：ロック画面にエノクの時計（リアルタイム）と聖書の日付、ホーム画面にはあなたの画像のみ。";
        l.uiSet = "壁紙に設定（ホーム画面とロック画面を選択）"; l.uiPick = "背景画像を選ぶ"; l.uiNoBg = "画像なし（既定の背景）";
        l.uiDial = "エノクの時計を表示"; l.uiDark = "ダークテーマ"; l.uiHome = "ホーム画面にも時計と日付を表示";
        l.uiHeight = "表示位置の高さ（サムスンの時計と重ならないように）"; l.uiLoc = "日の出・日の入りの計算地点";
        l.uiSaveLoc = "地点を保存"; l.uiSaved = "保存しました"; l.uiBadLoc = "座標が正しくありません";
        l.uiBadImg = "画像を読み込めませんでした"; l.uiLang = "言語";
        return l;
    }
}
