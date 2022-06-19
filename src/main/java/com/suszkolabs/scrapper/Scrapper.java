package com.suszkolabs.scrapper;

import com.suszkolabs.entity.Review;
import com.suszkolabs.entity.Teacher;
import com.suszkolabs.utils.ScrapperInitializationException;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.jsoup.Jsoup;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class Scrapper {


    private final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.5005.115 Safari/537.36";
    private final String POLWRO_USERNAME = System.getenv("usernamePolwro");
    private final String POLWRO_PASSWORD = System.getenv("passwordPolwro");

    private final String DATABASE_USERNAME = System.getenv("usernameDatabase");
    private final String DATABASE_PASSWORD = System.getenv("passwordDatabase");
    private final String DATABASE_CONNECTION_LINK = System.getenv("databaseConnectionLink");
    private final java.sql.Connection connection;

    private final String URL_PREFIX = "https://polwro.com/";
    private final String MATEMATYCY_URL = "https://polwro.com/f,matematycy,6";
    private final String FIZYCY_URL = "https://polwro.com/f,fizycy,7";
    private final String INFORMATYCY_URL = "https://polwro.com/f,informatycy,25";
    private final String CHEMICY_URL = "https://polwro.com/f,chemicy,8";
    private final String ELEKTRONICY_URL = "https://polwro.com/f,elektronicy,9";
    private final String JEZYKOWCY_URL = "https://polwro.com/f,jezykowcy,10";
    private final String HUMANISCI_URL = "https://polwro.com/f,humanisci,11";
    private final String SPORTOWCY_URL = "https://polwro.com/f,sportowcy,12";
    private final String POZOSTALI_URL = "https://polwro.com/f,inni,42";


    private final Map<String, Integer> titlePriorities;
    private final Map<String, String> formData;
    private Connection.Response login;

    /**
     * Initializes Scrapper, before initialization make sure proper env parameters are set
     * [polwroUsername, polwroPassword, databaseUsername, databasePassword, databaseConnectionLink]
     */
    public Scrapper(){
        titlePriorities = new HashMap<>();
        titlePriorities.put("prof", 1);
        titlePriorities.put("dr", 2);
        titlePriorities.put("hab", 3);
        titlePriorities.put("doc", 4);
        titlePriorities.put("mgr", 5);
        titlePriorities.put("inz", 6);
        titlePriorities.put("inż", 7);

        this.formData = new HashMap<>();
        formData.put("username", POLWRO_USERNAME);
        formData.put("password", POLWRO_PASSWORD);
        formData.put("login", "Zaloguj");

        try {
            Connection.Response loginHome = Jsoup.connect("https://polwro.com/login.php?redirect=viewforum.php&f=6&start=0")
                    .method(Connection.Method.GET)
                    .userAgent(USER_AGENT)
                    .execute();

            login = Jsoup.connect("https://polwro.com/login.php?redirect=viewforum.php&f=6&start=0")
                    .method(Connection.Method.POST)
                    .data(formData)
                    .cookies(loginHome.cookies())
                    .userAgent(USER_AGENT)
                    .followRedirects(true)
                    .execute();

            connection = DriverManager.getConnection(DATABASE_CONNECTION_LINK, DATABASE_USERNAME, DATABASE_PASSWORD);
        }catch(IOException | SQLException e){
            e.printStackTrace();
            throw new ScrapperInitializationException(formData);
        }

    }

    /** Builds database script(s) depending on isUpdate attribute
     *
     * @param isUpdate if true generates update scripts (without schema, update prepared through INSERT IGNORE) for each of the categories separately,
     *                      otherwise generates database setup script (bulk file with all insertions and schema). Each script is transactional.
     * @param execute if true uses apache ibatis scriptrunner and runs sql scripts on the provided database
     * @throws SQLException insertions are setup through PreparedStatement in order to avoid SQL injection
     * @throws IOException may be caused when writing data to .sql file
     * @throws InterruptedException threads are used to provide timeout between requests
     */
    public void buildDatabaseScript(boolean isUpdate, boolean execute) throws SQLException, IOException, InterruptedException {
        buildDatabaseScript(login, isUpdate);

        if(execute){
            ScriptRunner scriptRunner = new ScriptRunner(connection);
            if(isUpdate){
                List<String> suffixes = List.of("matematycy", "fizycy", "informatycy", "chemicy", "elektronicy",
                        "jezykowcy", "humanisci", "sportowcy", "pozostali");
                for(String suffix: suffixes)
                    scriptRunner.runScript(new FileReader("src/main/resources/opinie_update_" + suffix + ".sql"));
            }else {
                scriptRunner.runScript(new FileReader("src/main/resources/opinie_setup_schema.sql"));
                scriptRunner.runScript(new FileReader("src/main/resources/opinie_setup_data.sql"));
            }
        }
    }

    private void buildDatabaseScript(Connection.Response login, boolean isUpdate) throws IOException, InterruptedException, SQLException {
        final String MATEMATYCY_URL = "https://polwro.com/f,matematycy,6";
        final String FIZYCY_URL = "https://polwro.com/f,fizycy,7";
        final String INFORMATYCY_URL = "https://polwro.com/f,informatycy,25";
        final String CHEMICY_URL = "https://polwro.com/f,chemicy,8";
        final String ELEKTRONICY_URL = "https://polwro.com/f,elektronicy,9";
        final String JEZYKOWCY_URL = "https://polwro.com/f,jezykowcy,10";
        final String HUMANISCI_URL = "https://polwro.com/f,humanisci,11";
        final String SPORTOWCY_URL = "https://polwro.com/f,sportowcy,12";
        final String POZOSTALI_URL = "https://polwro.com/f,inni,42";

        java.sql.Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/test_opinie", "root", "admin");

        List<String> categoryURLs = List.of(SPORTOWCY_URL, MATEMATYCY_URL, FIZYCY_URL, INFORMATYCY_URL,
                CHEMICY_URL, ELEKTRONICY_URL, JEZYKOWCY_URL, HUMANISCI_URL, SPORTOWCY_URL, POZOSTALI_URL);
        List<String> categoryNames = List.of("sportowcy", "matematycy", "fizycy", "informatycy", "chemicy", "elektronicy",
                "językowcy", "humaniści", "sportowcy", "pozostali");

        for(int i = 0; i < categoryURLs.size(); i++){
            String URL = categoryURLs.get(i);
            String category = categoryNames.get(i);

            // INFO: all pages from certain category
            List<String> teacherPagesURLs = extractTeacherPaginationURLs(URL, login);

            // INFO: all teachers from certain category
            List<Teacher> teacherModels = extractTeacherModels(teacherPagesURLs, login, category);

            // INFO: reviews of all teachers from certain category
            fetchTeachersReviews(teacherModels, login);

            String fileName = "src/main/resources/opinie_setup_data.sql";

            if(isUpdate)
                fileName = "src/main/resources/opinie_update_" + category + ".sql";

            // INFO: part responsible for database setup script creation
            try(BufferedWriter bwriter = new BufferedWriter(new FileWriter(fileName, false))) {

                bwriter.write("START TRANSACTION;");

                bwriter.newLine();
                bwriter.newLine();

                for (Teacher teacher : teacherModels) {
                    String queryTeacher = "INSERT INTO teacher(category, full_name, academic_title, average_rating, details_link) VALUES(?, ?, ?, ?, ?)";
                    if(isUpdate)
                        queryTeacher = "INSERT IGNORE INTO teacher(category, full_name, academic_title, average_rating, details_link) VALUES(?, ?, ?, ?, ?)";

                    PreparedStatement ps = connection.prepareStatement(queryTeacher);
                    ps.setString(1, teacher.getCategory().replace("'", "\\'"));
                    ps.setString(2, teacher.getFullName().replace("'", "\\'"));
                    ps.setString(3, teacher.getAcademicTitle().replace("'", "\\'"));
                    ps.setDouble(4, teacher.getAverageRating());
                    ps.setString(5, teacher.getDetailsLink().replace("'", "\\'"));

                    bwriter.write(ps.toString().split("com.mysql.cj.jdbc.ClientPreparedStatement:")[1].trim() + ";");
                    bwriter.newLine();

                    for(Review review: teacher.getReviews()){
                        // INFO: avoids scam threads and a couple of poorly formatted ones (just a couple)
                        if(review.getCourseName().length() < 200 && review.getTitle().length() < 200) {
                            String queryReview = "INSERT INTO review(course_name, given_rating, title, review, reviewer, post_date, teacher_id)" +
                                    " VALUES(?, ?, ?, ?, ?, ?, ?)";
                            if(isUpdate)
                                queryReview = "INSERT IGNORE INTO review(course_name, given_rating, title, review, reviewer, post_date, teacher_id)" +
                                        " VALUES(?, ?, ?, ?, ?, ?, ?)";

                            ps = connection.prepareStatement(queryReview);
                            ps.setString(1, review.getCourseName().replace("'", "\\'"));
                            ps.setDouble(2, review.getGivenRating());
                            ps.setString(3, review.getTitle().replace("'", "\\'"));
                            ps.setString(4, review.getReview().replace("'", "\\'"));
                            ps.setString(5, review.getReviewer().replace("'", "\\'"));
                            ps.setString(6, review.getPostDate().replace("'", "\\'"));
                            ps.setInt(7, review.getTeacherId());

                            bwriter.write(ps.toString().split("com.mysql.cj.jdbc.ClientPreparedStatement:")[1].trim() + ";");
                            bwriter.newLine();
                        }
                    }

                    bwriter.newLine();
                    bwriter.newLine();

                }

                // INFO: date of latest refresh, used later to refresh opinions and teachers
                if(!isUpdate) {
                    String refreshQuery = "INSERT INTO refresh_data(refresh_date) VALUES(?)";
                    PreparedStatement ps = connection.prepareStatement(refreshQuery);
                    ps.setString(1, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));

                    bwriter.write(ps.toString().replace("com.mysql.cj.jdbc.ClientPreparedStatement:", "").trim() + ";");
                    bwriter.newLine();
                }

                bwriter.write("COMMIT;");

            }catch(IOException e){
                e.printStackTrace();
            }
        }

    }

    private List<Teacher> fetchTeachersReviews(List<Teacher> teachers, Connection.Response login) throws IOException, InterruptedException {

        for(Teacher teacher: teachers) {
            // INFO: fetch all review pages URLs starting from teacher homePage
            String firstURL = URL_PREFIX + teacher.getDetailsLink();
            List<String> urls = extractTeacherReviewsPaginationURLs(firstURL, login);

            System.out.println(urls);
            for (String URL : urls) {
                System.out.println(URL);
                Connection.Response reviewPage = Jsoup.connect(URL)
                        .method(Connection.Method.GET)
                        .userAgent(USER_AGENT)
                        .cookies(login.cookies())
                        .execute();

                Thread.sleep(700);

                Document doc = reviewPage.parse();
                Elements posts = doc.select(".gradient_post");

                for (Element elem : posts) {
                    Element postContents = elem.selectFirst(".postBody");
                    Element reviewBody = postContents.selectFirst("span[itemprop=\"reviewBody\"]");

                    double givenRating = Double.parseDouble(postContents.selectFirst("span[itemprop=\"ratingValue\"]")
                            .text().trim().replace(",", "."));
                    Elements titles = reviewBody.select("span[style=\"font-weight: bold\"]");

                    String courseName = "";
                    String title = "";

                    // INFO: code responsible for detecting alternative review formats
                    if(titles.size() >= 2) {
                        courseName = titles.get(0).text().trim();
                        title = titles.get(1).text().trim();
                    }else{
                        courseName = reviewBody.select(" > :first-child").text().trim();
                        title = reviewBody.select(" > :nth-child(4)").text().trim();
                    }

                    String review = reviewBody.text();
                    String reviewer = elem.select(".ll").get(1).select("span[itemprop=\"author\"]").text();

                    // WARNING: title and coursename modification have to be conducted AFTER review modification (review depends on those)
                    review = review.replaceFirst(Pattern.quote(courseName), "").replaceFirst(Pattern.quote(title), "").trim();
                    title = title.replaceFirst("Ocena opisowa:", "").replaceFirst("Descriptive rating:", "").trim();
                    courseName = courseName.replaceFirst("Kurs:", "").replaceFirst("Course:", "").trim();

                    Element postDateDiv = elem.selectFirst(".post_date");

                    // INFO: date is stored as string because of the format but it may be stored as LocalDateTime as well
                    String replaceText = postDateDiv.selectFirst("a").text();
                    List<Integer> dateParts = Arrays.stream(postDateDiv.text().replace(replaceText, "")
                                    .trim()
                                    .split("[,s+:-]"))
                                    .map(e -> Integer.parseInt(e.trim()))
                                    .toList();
                    LocalDateTime dateTime = LocalDateTime.of(dateParts.get(0), dateParts.get(1), dateParts.get(2), dateParts.get(3), dateParts.get(4));
                    String postDate = dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

                    Review finalReview = new Review(courseName, givenRating, title, review, reviewer, postDate, teacher.getId());
                    teacher.addReview(finalReview);
                }
                Thread.sleep(400);
            }
        }
        return teachers;
    }

    private List<String> extractTeacherReviewsPaginationURLs(String pageURL, Connection.Response login) throws IOException, InterruptedException {
        Connection.Response homePage = Jsoup.connect(pageURL)
                .method(Connection.Method.GET)
                .cookies(login.cookies())
                .userAgent(USER_AGENT)
                .execute();

        Thread.sleep(300);

        Document doc = homePage.parse();

        List<String> finalHrefs = new ArrayList<>();
        finalHrefs.add(pageURL);

        Optional<Element> elem = Optional.ofNullable(doc.selectFirst(".pagination"));

        elem.ifPresent(element -> {
            List<String> hrefs = element.select(".postmenu").stream()
                    .map(e -> e.attr("href"))
                    .collect(Collectors.toList());

            System.out.println("LOG: HREFS = " + hrefs);

            // INFO: "next page" button generates duplicate URL
            if(hrefs.size() > 1) {
                hrefs.remove(hrefs.size() - 1);
            }

            // INFO: different strategy taken when there is more than 7 hrefs (paging differs)
            if(hrefs.size() > 2) {
                hrefs.remove(hrefs.size() - 1);
                String teacherHref = pageURL.split(URL_PREFIX)[1];

                String hrefBegin = hrefs.get(0);
                finalHrefs.add(hrefBegin);

                String hrefEnd = hrefs.get(hrefs.size() - 1);

                int start = Integer.parseInt(hrefBegin.split("start=")[1]);
                int end = Integer.parseInt(hrefEnd.split("start=")[1]);

                int difference = end - start;

                for (int i = 25; i < difference; i += 25)
                    finalHrefs.add(URL_PREFIX + teacherHref + "start=" + (start + i));

                finalHrefs.add(hrefEnd);
            }else{
                finalHrefs.addAll(hrefs);
            }
        });

        System.out.println("LOG: FINALHREFS = " + finalHrefs);

        return finalHrefs;
    }

    private List<String> extractTeacherPaginationURLs(String pageURL, Connection.Response login) throws IOException {
        Connection.Response homePage = Jsoup.connect(pageURL)
                .method(Connection.Method.GET)
                .cookies(login.cookies())
                .userAgent(USER_AGENT)
                .execute();

        Document doc = homePage.parse();

        Elements elem = doc.select(".pagination").first().select(".postmenu");

        List<String> finalHrefs = new ArrayList<>();
        finalHrefs.add(pageURL);

        List<String> hrefs = elem.stream()
                .map(e -> String.format("%s%s", URL_PREFIX, e.attr("href")))
                .collect(Collectors.toList());

        // "next page" button generates duplicate link
        if(hrefs.size() > 1)
            hrefs.remove(hrefs.size() - 1);

        // INFO: different strategy taken when there is more than 7 hrefs (paging differs)
        if(hrefs.size() > 2) {
            hrefs.remove(hrefs.size() - 1);
            String teacherHref = hrefs.get(0).split(URL_PREFIX)[1].split("&start=")[0];

            String hrefBegin = hrefs.get(0);
            finalHrefs.add(hrefBegin);

            String hrefEnd = hrefs.get(hrefs.size() - 1);

            int start = Integer.parseInt(hrefBegin.split("start=")[1]);
            int end = Integer.parseInt(hrefEnd.split("start=")[1]);

            int difference = end - start;

            for (int i = 25; i < difference; i += 25)
                finalHrefs.add(URL_PREFIX + teacherHref + "start=" + (start + i));

            finalHrefs.add(hrefEnd);
        }else{
            finalHrefs.addAll(hrefs);
        }

        System.out.println("LOG: FINALHREFS = " + finalHrefs);
        return finalHrefs;
    }

    private List<Teacher> extractTeacherModels(List<String> teacherPaginationURLs, Connection.Response login, String category) throws IOException, InterruptedException {
        List<Teacher> teachers = new ArrayList<>();
        String[] academicTitles = {"doc", "Doc", "mgr", "Mgr", "inż", "Inż", "inz", "Inz", "hab", "Hab", "dr", "Dr", "prof", "Prof"};

        for(String URL: teacherPaginationURLs){
            Connection.Response teachersPage = Jsoup.connect(URL)
                    .method(Connection.Method.GET)
                    .cookies(login.cookies())
                    .userAgent(USER_AGENT)
                    .execute();

            Thread.sleep(200);

            Document document = teachersPage.parse();
            Elements teacherDivs = document.select(".img.folder, .img.folder_hot");

            for(Element element: teacherDivs){
                Teacher teacher = new Teacher();
                teacher.setCategory(category);

                double averageRating = Double.parseDouble(element.select("> div:nth-child(1)").text().replace(',','.'));
                teacher.setAverageRating(averageRating);

                Element href = element.selectFirst(".vf");

                String detailsLink = href.attr("href");
                teacher.setDetailsLink(detailsLink);

                String teacherDetails = href.text();

                teacherDetails = teacherDetails.replace(".", "")
                        .replace(",", "")
                        .replace(";", "")
                        .replace(":", "")
                        .replace("'", "");

                List<String> containedTitles = new ArrayList<>();

                for(String title: academicTitles){
                    int titleIndex = teacherDetails.indexOf(title);
                    if(titleIndex != -1){
                        teacherDetails = teacherDetails.replace(title, "").trim();
                        containedTitles.add(title.toLowerCase(Locale.ROOT));
                    }
                }

                String fullTitle = constructFullTitle(containedTitles);

                teacher.setAcademicTitle(fullTitle.trim());
                teacher.setFullName(teacherDetails);

                int currentId = Teacher.getCurrentId();
                Teacher.setCurrentId(currentId + 1);
                teacher.setId(currentId);

                // in case of duplicate thread (it happens sometimes)
                if(!teachers.contains(teacher))
                    teachers.add(teacher);
            }
            Thread.sleep(400);
        }
        return teachers;
    }

    private String constructFullTitle(List<String> titleParts){
        String fullTitle = "";
        Map<Integer, String> titlePartPriorities = new TreeMap<>();

        for(String title: titleParts)
            titlePartPriorities.put(titlePriorities.get(title), title);

        for(String titlePart: titlePartPriorities.values()) {
            fullTitle += titlePart + " ";
        }
        return fullTitle.trim();
    }
}
