<script>
    import Header from "$lib/Header.svelte";
    import Footer from "$lib/Footer.svelte";
</script>

<Header />
<div class="success">
    <img src="/images/cart/machine1_2.gif" alt="Arbeitende Maschine"> <!--https://cdn.dribbble.com/users/119313/screenshots/1675522/machine1_2.gif-->
    <h1>Vielen Dank für deinen Einkauf!</h1>
    <h2>Deine Bestellung wird nun versandfertig gemacht</h2>
    <h3>Wir benachrichtigen dich, sobald deine Bestellung versand worden ist!</h3>
    <a href="/account/orders/">Meine Bestellungen ansehen</a>
</div>
<Footer />

<style>
    * {
        padding: 0;
        margin: 0;
        color: #3b332b;
    }
    .success {
        height: 50rem;
        text-align: center;
        display: flex;
        flex-direction: column;
        justify-content: flex-start;
        align-items: center;
        margin-top: 30px;
    }
    img {
        width: 450px;
        height: auto;
    }
    h1 {
        margin-top: 30px;
        margin-bottom: 15px;
        color: #3e704b;
        font-weight: bold;
    }
    h2, h3 {
        margin-bottom: 15px;
    }
    a {
        text-decoration: underline;
        font-size: 20px;
    }
</style>