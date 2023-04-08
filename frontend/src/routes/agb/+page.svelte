<script>
	import Header from "$lib/Header.svelte"
    import Footer from "$lib/Footer.svelte";
</script>

<svelte:head>
    <title>AGBs - DogNeeds</title>
</svelte:head>

<Header />
<div class="box">
    <div class="impressum">
        <h1>Allgemeine Geschäftsbedingungen</h1>
        <h2>§1 Notwendigkeit</h2>
        <p>Da in unserem Online-Shop keine Produkte versandt werden und keine Abbuchungen vorgenommen werden, verzichten wir auf die Erstellung von AGBs.</p>
        <h2>§2 Rückgaberecht</h2>
        <p>Da keine Waren verkauft werden, besteht auch kein Recht auf Rückgabe!</p>
        <h2>§3 Speicherung von Bestellungen</h2>
        <p>Bestellungen werden gespeichert, sobald man im Einkaufswagen "Zur Kasse" auswählt! Ebenfalls wird eine E-Mail als Bestellbestätigung versendet.</p>

    </div>
</div>
<Footer />

<style>
    .box {
        width: 100%;
        height: 100%;
        margin-top: 10px;
        margin-bottom: 5%;
        justify-content: center;
    }
    .impressum {
        width: 80%;
        padding: 5px;
        border: 2px solid #ededed;
        border-radius: 10px;
        margin: 0px auto;
        left: 0;
        right: 0;
    }
</style>