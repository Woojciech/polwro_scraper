package com.suszkolabs.scrapper;

import com.suszkolabs.entity.Teacher;
import org.jsoup.Jsoup;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
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
        System.out.println(URLs);
        List<Teacher> teacherModels = extractTeacherModels(URLs, login, "matematycy");
        teacherModels.forEach(System.out::println);

        // TODO - add equals for teacher to prevent duplicate threads
    }

    public static List<String> extractTeacherPaginationURLs(String pageUrl, Connection.Response login) throws IOException {
        Connection.Response homePage = Jsoup.connect(pageUrl)
                .method(Connection.Method.GET)
                .cookies(login.cookies())
                .userAgent(USER_AGENT)
                .execute();

        Document doc = homePage.parse();

        Elements elem = doc.select(".pagination").first().select(".postmenu");

        List<String> hrefs = elem.stream().map(e -> String.format("%s%s", URL_PREFIX, e.attr("href"))).collect(Collectors.toList());

        // add starting url (not present in pagination)
        hrefs.add(0, pageUrl);

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
