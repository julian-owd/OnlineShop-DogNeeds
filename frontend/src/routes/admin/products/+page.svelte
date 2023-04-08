<script>
    import Header from "$lib/Header.svelte";
    import Footer from "$lib/Footer.svelte";
    import { serverAddress } from "$lib/stores.js";
    import { browser } from "$app/environment";
    import { goto } from "$app/navigation";
    import { onMount } from "svelte";
    import { page } from "$app/stores";

    const session = browser ? window.sessionStorage.getItem("session") ?? "" : ""; // session from cookies
    const successColor = "#9de683";
    const errorColor = "#fa6969";

    let products = [];
    let resultId;

    onMount(async () => {
        // check admin
        let response = await fetch("http://" + serverAddress + "/account/admingroup", {
            method: "POST",
            body: session
        });
        let result = await response.text();
        if (result !== "true") {
            goto("/account/login?l=2");
        }

        // load products
        response = await fetch("http://" + serverAddress + "/product/get", {
            method: "POST"
        });
        result = JSON.parse(await response.text());
        
        // save products
        for (let i = 0; i < result.length; i++) {
            products[i] = {id: result[i].pid, name: result[i].name, description: result[i].description, price: result[i].price, category: (result[i].cid != null ? result[i].cid.name : "null")};
        }

        resultId = $page.url.searchParams.get("r");
    });
</script>

<svelte:head>
    <title>Produkte verwalten - DogNeeds</title>
</svelte:head>

<Header />
<div class="admin">
    {#if resultId == 1}
        <div class="result" style="background-color: {successColor}">
            <h1>Info</h1>
            <p>Das Produkt wurde erfolgreich gelöscht!</p>
        </div>
    {:else if resultId == 2}
        <div class="result" style="background-color: {errorColor}">
            <h1>Fehler</h1>
            <p>Das Produkt konnte nicht gelöscht werden!</p>
        </div>
    {:else if resultId == 3}
        <div class="result" style="background-color: {successColor}">
            <h1>Info</h1>
            <p>Das Produkt wurde erfolgreich erstellt!</p>
        </div>
    {:else if resultId == 4}
        <div class="result" style="background-color: {errorColor}">
            <h1>Fehler</h1>
            <p>Das Produkt konnte nicht erstellt werden!</p>
        </div> 
    {/if}
    <a href="/admin/">Admin-Bereich</a>
    <div class="title">
        <h2>Produkte</h2>
        <button on:click={() => goto("/admin/products/add")}>
            <img src="/images/admin/add.png" alt="Produkt hinzufügen"> <!--https://img.icons8.com/ios-filled/512/plus-2-math.png-->
        </button>
    </div>
    <div class="products">
        <table>
            <tr>
                <th>Name</th>
                <th>Beschreibung</th>
                <th>Preis</th>
                <th>Bearbeiten</th>
            </tr>
            {#each products as product}
                <tr style="background-color: {product.category === "null" ? "#ffa9a3" : "white"}">
                    <td>{product.name}</td>
                    <td>{product.description}</td>
                    <td>{product.price.toFixed(2)} €</td>
                    <td><button on:click={() => goto("/admin/products/edit?p=" + product.id)}><img src="/images/admin/edit.png" alt="Produkt bearbeiten" class="edit"></button></td> <!--https://img.icons8.com/ios-filled/512/pencil.png-->
                </tr>
            {/each}
        </table>
    </div>
</div>
<Footer />

<style>
    * {
        color: #514538;
    }
    .admin {
        display: grid;
        margin-left: 5%;
        margin-bottom: 4%;
        justify-content: center;
        text-align: center;
    }
    .result {
        border-radius: 10px;
        padding: 5px;
        margin-top: 20px;
    }
    .result h1, p {
        color: white;
    }
    .title {
        display: flex;
        justify-content: center;
    }
    .products {
        justify-content: center;
    }
    a {
        font-size: 42px;
        text-decoration: none;
        font-weight: bold;
        margin-top: 0.67em;
        margin-bottom: 0.67em;
    }
    button {
        background-color: white;
        border: none;
    }
    button:hover {
        cursor: pointer;
    }
    .title img {
        width: 40px;
        height: auto;
    }
    table {
        width: 100%;
        border: 1px solid black;
        border-collapse: collapse; /*make a single border instead of each cell having their own border*/
        margin-left: auto;
        margin-right: auto;
    }
    table, tr, th, td {
        border: 1px solid black;
    }
    table th {
        padding: 10px;
        background-color: antiquewhite;
    }
    table td {
        padding: 5px;
        word-wrap: break-word;
        min-width: 80px;
    }
    .edit {
        width: 32px;
        height: auto;
        padding: 0;
    }
</style>