package com.bot01.helloworld;

import com.bot01.helloworld.model.Enquiry;
import com.bot01.helloworld.repository.EnquiryRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@RestController
public class WebhookController {

      private final EnquiryRepository enquiryRepository;

public WebhookController(EnquiryRepository enquiryRepository) {
    this.enquiryRepository = enquiryRepository;
}

    @Value("${whatsapp.phone-number-id}")
    private String phoneNumberId;

    @Value("${whatsapp.access-token}")
    private String accessToken;

    private final String verifyToken = "mybot123";
     // Admin notification number
private final String adminNumber = "254795674437";
        // Stores the current conversation step for each customer
private final Map<String, String> customerState = new java.util.HashMap<>();

// Stores the customer's name
private final Map<String, String> customerName = new java.util.HashMap<>();

// Stores the selected service
private final Map<String, String> customerService = new java.util.HashMap<>();

    // Test endpoint
    @GetMapping("/")
    public String hello() {
        return "Hello World! My bot is running.";
    }

    // Meta webhook verification
    @GetMapping("/webhook")
    public String verifyWebhook(
            @RequestParam(name = "hub.mode", required = false) String mode,
            @RequestParam(name = "hub.verify_token", required = false) String token,
            @RequestParam(name = "hub.challenge", required = false) String challenge) {

        System.out.println("MODE: " + mode);
        System.out.println("TOKEN: " + token);
        System.out.println("CHALLENGE: " + challenge);

        if ("subscribe".equals(mode) && verifyToken.equals(token)) {
            return challenge;
        }

        return "Verification failed";
    }

    // Receive WhatsApp webhook events
    @PostMapping("/webhook")
    public String receiveMessage(
            @RequestBody Map<String, Object> payload) {

        System.out.println("====================================");
        System.out.println("WhatsApp webhook received");
        System.out.println(payload);
        System.out.println("====================================");

        try {

            // Get entry
            Object entryObject = payload.get("entry");

            if (!(entryObject instanceof List<?> entries)
                    || entries.isEmpty()) {

                System.out.println("No entry found.");
                return "EVENT_RECEIVED";
            }

            Object firstEntryObject = entries.get(0);

            if (!(firstEntryObject instanceof Map<?, ?>)) {
                System.out.println("Invalid entry.");
                return "EVENT_RECEIVED";
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> entry =
                    (Map<String, Object>) firstEntryObject;

            // Get changes
            Object changesObject = entry.get("changes");

            if (!(changesObject instanceof List<?> changes)
                    || changes.isEmpty()) {

                System.out.println("No changes found.");
                return "EVENT_RECEIVED";
            }

            Object firstChangeObject = changes.get(0);

            if (!(firstChangeObject instanceof Map<?, ?>)) {
                System.out.println("Invalid change.");
                return "EVENT_RECEIVED";
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> change =
                    (Map<String, Object>) firstChangeObject;

            // Get value
            Object valueObject = change.get("value");

            if (!(valueObject instanceof Map<?, ?>)) {
                System.out.println("No value found.");
                return "EVENT_RECEIVED";
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> value =
                    (Map<String, Object>) valueObject;

            // Get messages
            Object messagesObject = value.get("messages");

            /*
             * IMPORTANT:
             * Some webhook events do not contain "messages".
             * For example, delivery/read/failed status events.
             */
            if (!(messagesObject instanceof List<?> messages)
                    || messages.isEmpty()) {

                System.out.println(
                        "Webhook received, but it contains no incoming message."
                );

                return "EVENT_RECEIVED";
            }

            Object firstMessageObject = messages.get(0);

            if (!(firstMessageObject instanceof Map<?, ?>)) {
                System.out.println("Invalid message.");
                return "EVENT_RECEIVED";
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> whatsappMessage =
                    (Map<String, Object>) firstMessageObject;

            // Get sender
            String from = String.valueOf(
                    whatsappMessage.get("from")
            );

            // Get message type
            String type = String.valueOf(
                    whatsappMessage.get("type")
            );

            System.out.println("From: " + from);
            System.out.println("Message type: " + type);

            // We currently handle text messages
            if ("text".equals(type)) {

                Object textObject =
                        whatsappMessage.get("text");

                if (!(textObject instanceof Map<?, ?>)) {

                    System.out.println(
                            "Text message has no text body."
                    );

                    return "EVENT_RECEIVED";
                }

                @SuppressWarnings("unchecked")
                Map<String, Object> text =
                        (Map<String, Object>) textObject;

                String messageText =
                        String.valueOf(text.get("body"));

                System.out.println(
                        "Message: " + messageText
                );
// Create bot reply
String reply;

String command = messageText.trim().toLowerCase();

// ==========================================
// CUSTOMER ENQUIRY / CONTACT FLOW
// ==========================================

// Step 1: Customer is entering their name
if ("ASK_NAME".equals(customerState.get(from))) {

    customerName.put(from, messageText.trim());

    customerState.put(from, "ASK_SERVICE");

    reply = """
            Nice to meet you! 👋

            Which service do you need?

            1️⃣ Website Development
            2️⃣ WhatsApp Bot
            3️⃣ ICT Support
            4️⃣ Other
            """;

    sendWhatsAppMessage(from, reply);
    return "EVENT_RECEIVED";
}


// Step 2: Customer is selecting a service
if ("ASK_SERVICE".equals(customerState.get(from))) {

    String selectedService;

    switch (command) {

        case "1":
            selectedService = "Website Development";
            break;

        case "2":
            selectedService = "WhatsApp Bot";
            break;

        case "3":
            selectedService = "ICT Support";
            break;

        case "4":
            selectedService = "Other";
            break;

        default:
            sendWhatsAppMessage(
                    from,
                    "Please choose 1, 2, 3, or 4."
            );
            return "EVENT_RECEIVED";
    }

    customerService.put(from, selectedService);

    customerState.put(from, "ASK_REQUEST");

    reply = """
            Great! 👍

            Please describe what you need.

            For example:
            "I need a website for my school."
            """;

    sendWhatsAppMessage(from, reply);
    return "EVENT_RECEIVED";
}


// Step 3: Customer describes their request
if ("ASK_REQUEST".equals(customerState.get(from))) {

    String name = customerName.get(from);
    String service = customerService.get(from);

    reply = """
            ✅ REQUEST RECEIVED

            Name: %s
            Service: %s
            Request: %s

            Thank you! 🙏
            We will get back to you shortly.
            """.formatted(
                    name,
                    service,
                    messageText.trim()
            );
     // Save enquiry to database
    Enquiry enquiry = new Enquiry(
        from,
        name,
        service,
        messageText.trim(),
        "NEW"
    );

  enquiryRepository.save(enquiry);

  System.out.println("✅ Enquiry saved to database.");
// Notify admin about the new enquiry
String adminNotification = """
        🔔 NEW CUSTOMER ENQUIRY

        👤 Name: %s
        📱 Phone: %s
        🌐 Service: %s

        📝 Request:
        %s

        🕐 Time: %s
        """.formatted(
                name,
                from,
                service,
                messageText.trim(),
                enquiry.getCreatedAt()
        );

sendWhatsAppMessage(adminNumber, adminNotification);

System.out.println("🔔 Admin notification sent.");
    // Clear the conversation state
    customerState.remove(from);
    customerName.remove(from);
    customerService.remove(from);

    sendWhatsAppMessage(from, reply);
    return "EVENT_RECEIVED";
}

switch (command) {

    // =========================
    // MAIN MENU
    // =========================

    case "hello":
    case "hi":
    case "hey":
    case "menu":
    case "0":

        reply = """
                👋 WELCOME TO PETER'S BOT

                How can we help you today?

                1️⃣ ICT Services
                2️⃣ WhatsApp Bot Services
                3️⃣ Website Development
                4️⃣ About Us
                5️⃣ Contact Us

                Reply with a number from 1 to 5.
                """;
        break;


    // =========================
    // ICT SERVICES
    // =========================

    case "1":

        reply = """
                💻 ICT SERVICES

                Choose a service:

                1️⃣ Computer Support
                2️⃣ Software Installation
                3️⃣ Networking
                4️⃣ ICT Consultation

                Reply 0️⃣ for Main Menu, 1.1 to 1.4 t  for service.
                """;
        break;


    case "1.1":

        reply = """
                🖥️ COMPUTER SUPPORT

                We can help with:

                • Computer troubleshooting
                • System errors
                • Performance problems
                • Software problems
                • General computer support

                📩 Send us a description of your problem.

                Reply MENU for Main Menu.
                """;
        break;


    case "1.2":

        reply = """
                💿 SOFTWARE INSTALLATION

                We provide assistance with:

                • Operating systems
                • Applications
                • Drivers
                • Development software
                • Software configuration

                📩 Tell us which software you need.

                Reply MENU for Main Menu.
                """;
        break;


    case "1.3":

        reply = """
                🌐 NETWORKING

                Our networking services include:

                • Network setup
                • Wi-Fi configuration
                • Router configuration
                • Network troubleshooting
                • Basic network security

                📩 Tell us about your networking problem.

                Reply MENU for Main Menu.
                """;
        break;


    case "1.4":

        reply = """
                👨‍💻 ICT CONSULTATION

                We can provide guidance on:

                • ICT projects
                • Software solutions
                • Website projects
                • Digital services
                • Technology planning

                📩 Tell us what you want to build.

                Reply MENU for Main Menu.
                """;
        break;


    // =========================
    // WHATSAPP BOT SERVICES
    // =========================

    case "2":

        reply = """
                🤖 WHATSAPP BOT SERVICES

                Choose an option:

                1️⃣ Basic WhatsApp Bot
                2️⃣ Business WhatsApp Bot
                3️⃣ Advanced Automation

                Reply 0️⃣ for Main Menenu, 2.1 to 2.3 for Services.
                """;
        break;


    case "2.1":

        reply = """
                🤖 BASIC WHATSAPP BOT

                Includes:

                • Automatic replies
                • Welcome messages
                • Simple menus
                • Frequently asked questions

                Suitable for small projects.

                📩 Contact us for a quotation.

                Reply MENU for Main Menu.
                """;
        break;


    case "2.2":

        reply = """
                🏢 BUSINESS WHATSAPP BOT

                Includes:

                • Customer menus
                • Service information
                • Automated responses
                • Customer enquiries
                • Business information
                • Lead collection

                📩 Contact us for a quotation.

                Reply MENU for Main Menu.
                """;
        break;


    case "2.3":

        reply = """
                ⚡ ADVANCED AUTOMATION

                Advanced solutions can include:

                • Databases
                • Customer records
                • Orders
                • Notifications
                • Payment integration
                • AI-powered responses

                📩 Contact us to discuss your project.

                Reply MENU for Main Menu.
                """;
        break;


    // =========================
    // WEBSITE DEVELOPMENT
    // =========================

    case "3":

        reply = """
                🌐 WEBSITE DEVELOPMENT

                Choose a website type:

                1️⃣ Business Website
                2️⃣ School Website
                3️⃣ Portfolio Website
                4️⃣ E-commerce Website

                Reply 0️⃣ for Mainin menu,3.1 to 3.4 for type.
                """;
        break;


    case "3.1":

        reply = """
                🏢 BUSINESS WEBSITE

                A professional website for your business
                to showcase your services, contacts and
                products online.

                📩 Tell us about your business.

                Reply MENU for Main Menu.
                """;
        break;


    case "3.2":

        reply = """
                🎓 SCHOOL WEBSITE

                We can create websites featuring:

                • School information
                • Departments
                • Announcements
                • Gallery
                • Contact information

                📩 Tell us about your school.

                Reply MENU for Main Menu.
                """;
        break;


    case "3.3":

        reply = """
                👤 PORTFOLIO WEBSITE

                Showcase your:

                • Skills
                • Projects
                • Experience
                • Services
                • Contact information

                📩 Tell us what you want to showcase.

                Reply MENU for Main Menu.
                """;
        break;


    case "3.4":

        reply = """
                🛒 E-COMMERCE WEBSITE

                We can build websites with:

                • Product listings
                • Shopping features
                • Customer enquiries
                • Order management
                • Payment integration

                📩 Tell us what you want to sell.

                Reply MENU for Main Menu.
                """;
        break;


    // =========================
    // ABOUT US
    // =========================

    case "4":

        reply = """
                ℹ️ ABOUT US

                We provide ICT and digital solutions
                for individuals, businesses and organizations.

                Our goal is to make technology simple,
                useful and accessible.

                Reply MENU for Main Menu.
                """;
        break;


    // =========================
    // CONTACT
    // =========================
       case "5":

    customerState.put(from, "ASK_NAME");

    reply = """
            📞 CONTACT US

            Let's get your request started. 👍

            What is your name?
            """;

    break;


    // =========================
    // DEFAULT
    // =========================

    default:

        reply = """
                ❓ I didn't understand that.

                Please choose from the main menu:

                1️⃣ ICT Services
                2️⃣ WhatsApp Bot Services
                3️⃣ Website Development
                4️⃣ About Us
                5️⃣ Contact Us

                Type MENU to see this menu again.
                """;
        break;
}

sendWhatsAppMessage(from, reply);            } else {

                System.out.println(
                        "Received a non-text message: " + type
                );
            }

        } catch (Exception e) {

            System.out.println(
                    "Could not process message: "
                            + e.getMessage()
            );

            e.printStackTrace();
        }

        return "EVENT_RECEIVED";
    }

    // Send WhatsApp reply through Meta Cloud API
    private void sendWhatsAppMessage(
            String recipient,
            String text) {

        String url =
                "https://graph.facebook.com/v23.0/"
                        + phoneNumberId
                        + "/messages";

        System.out.println("====================================");
        System.out.println("Sending WhatsApp reply");
        System.out.println("Recipient: " + recipient);
        System.out.println("Phone Number ID: " + phoneNumberId);
        System.out.println("URL: " + url);
        System.out.println("====================================");

        RestTemplate restTemplate =
                new RestTemplate();

        HttpHeaders headers =
                new HttpHeaders();

        headers.setContentType(
                MediaType.APPLICATION_JSON
        );

        headers.setBearerAuth(accessToken);

        Map<String, Object> textBody =
                Map.of(
                        "body", text
                );

        Map<String, Object> body =
                Map.of(
                        "messaging_product", "whatsapp",
                        "to", recipient,
                        "type", "text",
                        "text", textBody
                );

        HttpEntity<Map<String, Object>> request =
                new HttpEntity<>(
                        body,
                        headers
                );

        try {

            ResponseEntity<String> response =
                    restTemplate.postForEntity(
                            url,
                            request,
                            String.class
                    );

            System.out.println(
                    "Meta response status: "
                            + response.getStatusCode()
            );

            System.out.println(
                    "Meta response body: "
                            + response.getBody()
            );

        } catch (Exception e) {

            System.out.println(
                    "Could not send WhatsApp reply:"
            );

            System.out.println(
                    e.getMessage()
            );
        }
    }
}

