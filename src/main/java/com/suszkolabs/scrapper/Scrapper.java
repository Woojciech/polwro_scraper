package com.suszkolabs.scrapper;

import com.squareup.okhttp.*;
import com.suszkolabs.entity.Teacher;
import org.jsoup.Jsoup;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import io.github.bonigarcia.wdm.WebDriverManager;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class Scrapper {

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.5005.115 Safari/537.36";
    private static final String USERNAME = System.getenv("username");
    private static final String PASSWORD = System.getenv("password");
    private static final String URL_PREFIX = "https://polwro.com/";


    public static void main(String[] args) throws IOException {

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

        /*
        List<String> URLS = List.of(MATEMATYCY_URL, FIZYCY_URL, CHEMICY_URL, ELEKTRONICY_URL,
                INFORMATYCY_URL, JEZYKOWCY_URL, HUMANISCI_URL, SPORTOWCY_URL, POZOSTALI_URL);

        URLS.stream().forEach(URL -> {
            try {
                System.out.println(extractReviewPages(URL, login) + "\n\n");
                Thread.sleep(350);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
         */
        List<String> mathematicianUrls = extractReviewPages(MATEMATYCY_URL, login);
        //System.out.println(mathematicianUrls);

        Connection.Response teacherPage = Jsoup.connect(mathematicianUrls.get(0))
                .method(Connection.Method.GET)
                .cookies(login.cookies())
                .userAgent(USER_AGENT)
                .execute();

        Document doc = teacherPage.parse();

        Elements teachers = doc.select(".img.folder, .img.folder_hot");

        // dla każdego z kontenerów zrób
        /*
            1. utwórz nauczyciela
            2. dodaj detailsLink
            3. dodaj imię, nazwisko i tytuł
         */

        Teacher teacher = new Teacher();
        teacher.setCategory("matematycy");

        Element test = teachers.get(0);
        double averageRating = Double.parseDouble(test.select("> div:nth-child(1)").text().replace(',','.'));
        teacher.setAverageRating(averageRating);

        Element href = test.selectFirst(".vf");

        String detailsLink = href.attr("href");
        teacher.setDetailsLink(detailsLink);

        String teacherDetails = href.text();

        List<String> filteredDetails = Arrays.stream(teacherDetails.split("[,s+]"))
                .filter(detail -> !detail.equals(" "))
                .map(String::trim)
                .toList();

        teacher.setFirstName(filteredDetails.get(0));
        teacher.setLastName(filteredDetails.get(1));
        teacher.setAcademicTitle(filteredDetails.get(2));

        System.out.println(teacher);

        Connection.Response reviewPage = Jsoup.connect(URL_PREFIX + teacher.getDetailsLink())
                .method(Connection.Method.GET)
                .cookies(login.cookies())
                .userAgent(USER_AGENT)
                .execute();

        System.out.println(reviewPage.body());
    }

    public static List<String> extractReviewPages(String pageUrl, Connection.Response login) throws IOException {
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
}
