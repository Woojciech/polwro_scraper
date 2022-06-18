package com.suszkolabs.scrapper;

import com.suszkolabs.entity.Review;
import com.suszkolabs.entity.Teacher;
import org.jsoup.Jsoup;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.*;
import java.util.stream.Collectors;

public class Scrapper {

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.5005.115 Safari/537.36";
    private static final String USERNAME = System.getenv("username");
    private static final String PASSWORD = System.getenv("password");
    private static final String URL_PREFIX = "https://polwro.com/";
    private static final Map<String, Integer> titlePriorities;

    static{
        titlePriorities = new HashMap<>();
        titlePriorities.put("prof", 1);
        titlePriorities.put("dr", 2);
        titlePriorities.put("hab", 3);
        titlePriorities.put("doc", 4);
        titlePriorities.put("mgr", 5);
        titlePriorities.put("inz", 6);
        titlePriorities.put("inż", 7);
    }


    public static void main(String[] args) throws IOException, InterruptedException {

        final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.5005.115 Safari/537.36";
        final String USERNAME = System.getenv("username");
        final String PASSWORD = System.getenv("password");

        // formData
        /*
        username: woojtek
        password: HashMap<String, String> f
        redirect: viewforum.php?f=6&start=0
        login: Zaloguj
         */
        HashMap<String, String> formData = new HashMap<>();
        formData.put("username", USERNAME);
        formData.put("password", PASSWORD);
        //formData.put("redirect", "viewforum.php?f=6&start=0");
        formData.put("login", "Zaloguj");

        // login part
        //_________________________________________________________________//
        Connection.Response loginHome = Jsoup.connect("https://polwro.com/login.php?redirect=viewforum.php&f=6&start=0")
                .method(Connection.Method.GET)
                .userAgent(USER_AGENT)
                .execute();

        Connection.Response login = Jsoup.connect("https://polwro.com/login.php?redirect=viewforum.php&f=6&start=0")
                .method(Connection.Method.POST)
                .data(formData)
                .cookies(loginHome.cookies())
                .userAgent(USER_AGENT)
                .followRedirects(true)
                .execute();
        //_________________________________________________________________//

        final String MATEMATYCY_URL = "https://polwro.com/f,matematycy,6";
        final String FIZYCY_URL = "https://polwro.com/f,fizycy,7";
        final String INFORMATYCY_URL = "https://polwro.com/f,informatycy,25";
        final String CHEMICY_URL = "https://polwro.com/f,chemicy,8";
        final String ELEKTRONICY_URL = "https://polwro.com/f,elektronicy,9";
        final String JEZYKOWCY_URL = "https://polwro.com/f,jezykowcy,10";
        final String HUMANISCI_URL = "https://polwro.com/f,humanisci,11";
        final String SPORTOWCY_URL = "https://polwro.com/f,sportowcy,12";
        final String POZOSTALI_URL = "https://polwro.com/f,inni,42";

        List<String> URLs = extractTeacherPaginationURLs(MATEMATYCY_URL, login);
        List<Teacher> teacherModels = extractTeacherModels(URLs, login, "matematycy");
        List<Teacher> teacherTest = List.of(teacherModels.get(1));
        System.out.println(fetchTeachersReviews(teacherTest, login));

        // TODO - add equals for teacher and prevent duplicate threads
        // TODO - add null mechanism for teacherPaging extraction
        // TODO - add last refresh
    }

    public static List<Teacher> fetchTeachersReviews(List<Teacher> teachers, Connection.Response login) throws IOException, InterruptedException {

        for(Teacher teacher: teachers) {
            // fetch all review pages URLs starting from teacher homePage
            String firstURL = URL_PREFIX + teacher.getDetailsLink();
            List<String> urls = extractTeacherReviewsPaginationURLs(firstURL, login);

            for (String URL : urls) {
                Connection.Response reviewPage = Jsoup.connect(URL)
                        .method(Connection.Method.GET)
                        .userAgent(USER_AGENT)
                        .cookies(login.cookies())
                        .execute();

                Thread.sleep(200);

                Document doc = reviewPage.parse();
                Elements posts = doc.select(".gradient_post");

                for (Element elem : posts) {
                    Element postContents = elem.selectFirst(".postBody");
                    Element reviewBody = postContents.selectFirst("span[itemprop=\"reviewBody\"]");

                    //String courseName = reviewBody.select(" > :first-child").text().trim();
                    double givenRating = Double.parseDouble(postContents.selectFirst("span[itemprop=\"ratingValue\"]")
                            .text().trim().replace(",", "."));
                    //String title = reviewBody.select(" > :nth-child(4)").text().trim();
                    Elements titles = reviewBody.select("span[style=\"font-weight: bold\"]");

                    String courseName = "";
                    String title = "";

                    // code responsible for detecting alternative review formats
                    if(titles.size() >= 2) {
                        courseName = titles.get(0).text().trim();
                        title = titles.get(1).text().trim();
                    }else{
                        courseName = reviewBody.select(" > :first-child").text().trim();
                        title = reviewBody.select(" > :nth-child(4)").text().trim();
                    }

                    String review = reviewBody.text();
                    String reviewer = elem.select(".ll").get(1).select("span").get(1).text();

                    // WARNING: title and coursename modification have to be conducted AFTER review modification (review depends on those)
                    review = review.replaceFirst(courseName, "").replaceFirst(title, "").trim();
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
                    String postDate = dateTime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));

                    Review finalReview = new Review(courseName, givenRating, title, review, reviewer, postDate, teacher.getId());

                    System.out.println(finalReview);
                    /*
                    System.out.println(courseName);
                    System.out.println(givenRating);
                    System.out.println(title);
                    System.out.println(review);
                    System.out.println(reviewer);
                    System.out.println(postDate);
                    System.out.println("//_______________________________________________________________________//\n\n");
                     */
                    teacher.addReview(finalReview);
                }
                Thread.sleep(400);
            }
        }
        return teachers;
    }

    public static List<String> extractTeacherReviewsPaginationURLs(String pageURL, Connection.Response login) throws IOException, InterruptedException {
        Connection.Response homePage = Jsoup.connect(pageURL)
                .method(Connection.Method.GET)
                .cookies(login.cookies())
                .userAgent(USER_AGENT)
                .execute();

        Thread.sleep(200);

        Document doc = homePage.parse();

        List<String> finalHrefs = new ArrayList<>();
        finalHrefs.add(pageURL);

        Optional<Element> elem = Optional.ofNullable(doc.selectFirst(".pagination"));
        elem.ifPresent(element -> {
            List<String> hrefs = element.select(".postmenu").stream().map(e -> e.attr("href")).collect(Collectors.toList());

            // "next page" button generates duplicate URL
            if(hrefs.size() > 1)
                hrefs.remove(hrefs.size() - 1);

            finalHrefs.addAll(hrefs);
        });

        return finalHrefs;
    }

    public static List<String> extractTeacherPaginationURLs(String pageURL, Connection.Response login) throws IOException {
        Connection.Response homePage = Jsoup.connect(pageURL)
                .method(Connection.Method.GET)
                .cookies(login.cookies())
                .userAgent(USER_AGENT)
                .execute();

        Document doc = homePage.parse();

        Elements elem = doc.select(".pagination").first().select(".postmenu");

        List<String> hrefs = elem.stream().map(e -> String.format("%s%s", URL_PREFIX, e.attr("href"))).collect(Collectors.toList());

        // presence of "next page" button creates duplicate link
        hrefs.remove(hrefs.size() - 1);

        // add starting url (not present in pagination)
        hrefs.add(0, pageURL);

        return hrefs;
    }

    public static List<Teacher> extractTeacherModels(List<String> teacherPaginationURLs, Connection.Response login, String category) throws IOException, InterruptedException {
        List<Teacher> teachers = new ArrayList<>();
        String[] academicTitles = {"doc", "Doc", "mgr", "Mgr", "inż", "Inż", "inz", "Inz", "hab", "Hab", "dr", "Dr", "prof", "Prof"};

        for(String URL: teacherPaginationURLs){
            Connection.Response teachersPage = Jsoup.connect(URL)
                    .method(Connection.Method.GET)
                    .cookies(login.cookies())
                    .userAgent(USER_AGENT)
                    .execute();

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

                if(!teachers.contains(teacher))
                    teachers.add(teacher);
            }
            Thread.sleep(400);
        }
        return teachers;
    }

    private static String constructFullTitle(List<String> titleParts){
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
