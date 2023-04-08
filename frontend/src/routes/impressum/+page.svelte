<script>
	import Header from "$lib/Header.svelte"
    import Footer from "$lib/Footer.svelte";
</script>

<svelte:head>
    <title>Impressum - DogNeeds</title>
</svelte:head>

<Header />
<div class="box">
    <div class="impressum">
        <div class="content">
            <h1>Impressum</h1>
        </div>
    </div>
    <div class="datenschutz">
        <div class="content">
            <h1>Datenschutzerklärung</h1>
            <h2>1. Name und Anschrift des für die Verarbeitung Verantwortlichen</h2>

            <h2>2. Erhebung und Speicherung personenbezogener Daten und deren Verwendung</h2>
            <p>
                Beim Aufrufen unserer Website werden durch den auf Ihrem Endgerät zum Einsatz kommenden Browser automatisch Informationen an den Server unserer Website gesendet. Diese Informationen werden temporär in einem sog. Logfile gespeichert. Folgende Informationen werden dabei ohne Ihr Zutun erfasst und bis zur automatisierten Löschung gespeichert:
            </p>
            <ul>
                <li>IP-Adresse</li>
                <li>Datum und Uhrzeit des Zugriffs</li>
                <li>Name und URL der abgerufenen Datei</li>
                <li>Website, von der aus der Zugriff erfolgt</li>
                <li>Verwendeter Browser und ggf. das Betriebssystem Ihres Rechners sowie der Name Ihres Access-Providers</li>
            </ul>
            <p>Die genannten Daten werden durch uns zu folgenden Zwecken verarbeitet:</p>
            <ul>
                <li>Gewährleistung eines reibungslosen Verbindungsaufbaus der Website</li>
                <li>Gewährleistung einer komfortablen Nutzung unserer Website</li>
                <li>Auswertung der Systemsicherheit und -stabilität</li>
            </ul>
            <h2>Google</h2>
            <ul>
                <li>Es werden Schriftarten von Google Fonts geladen</li>
            </ul>
            <h2>Cookies</h2>
            <p>Sollte im Browser nichts Gegenteiliges eingestellt sein, dann verwendet diese Seite so genannte Cookies. Sie dienen dazu, unser Angebot nutzerfreundlicher, effektiver und sicherer zu machen. Cookies sind kleine Textdateien, die auf dem Rechner abgelegt werden und die der Browser speichert. Diese Cookies richten auf einem Rechner keinen Schaden an und enthalten keine Viren. Das/die Popup Fenster der Startseite sendet/senden ggf. einen Cookie, um dieses Fenster nicht wiederholt anzuzeigen.</p>
        </div>
    </div>
</div>
<Footer />

<style>
    span {
        font-weight: bold;
        font-size: 16px;
    }
    a {
        text-decoration: none;
        color: #333333;
    }
    .box {
        width: 100%;
        height: 100%;
        margin-top: 10px;
        margin-bottom: 5%;
        justify-content: center;
    }
    .impressum, .datenschutz {
        width: 80%;
        padding: 5px;
        border: 2px solid #ededed;
        border-radius: 10px;
        margin: 0px auto;
        left: 0;
        right: 0;
    }
    .datenschutz {
        margin-top: 10px;
    }
    .box {
        margin-left: 15px;
    }
</style>