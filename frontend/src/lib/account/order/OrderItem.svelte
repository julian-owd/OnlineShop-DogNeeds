<script>
    import LoadingBar from "$lib/LoadingBar.svelte";
    import Error from "$lib/Error.svelte";
    import { serverAddress, defaultProductPicture } from "$lib/stores.js";
    import { browser } from "$app/environment";
    import { onDestroy } from "svelte";

    const session = browser ? window.sessionStorage.getItem("session") ?? "" : ""; // session from cookies

    export let orderId;
    let error;
    let timestamp;
    let price;
    let products;

    let displayDetails = "none";
    let buttonImage = "/images/account/expand.png";

    /**
     * load the details of the order
     */
    async function loadOrder() {
        let request = await fetch("http://" + serverAddress + "/order/getOrder", {
            method: "POST",
            body: JSON.stringify({
                session,
                orderId
            })
        }).catch(function () {
            return;
        });
        let result = JSON.parse(await request.text());
        if (!result.timestamp) {
            error = "Diese Bestellung konnte nicht geladen werden!";
            return;
        }
        
        var time = new Date(result.timestamp);
        timestamp = (time.getDate() < 10 ? "0" + time.getDate() : time.getDate()) + ".";
        timestamp += (time.getMonth() < 10 ? "0" + (time.getMonth() + 1) : (time.getMonth() + 1)) + ".";
        timestamp += time.getFullYear() + " ";
        timestamp += (time.getHours() < 10 ? "0" + time.getHours() : time.getHours()) + ":";
        timestamp += (time.getMinutes() < 10 ? "0" + time.getMinutes() : time.getMinutes()) + " Uhr";
        price = result.price;
        products = result.products;
        
        for (var i = 0; i < products.length; i++) {
            if (products[i].pid) {
                request = await fetch("http://" + serverAddress + "/product/img", {
                    method: "POST",
                    body: products[i].pid.pid
                });
                result = await request.blob();
                if (result.size > 0) {
                    products[i].pid.img = URL.createObjectURL(result);
                } else {
                    products[i].pid.img = defaultProductPicture;
                }
            } else {
            }
        }
    }

    /**
     * expand / reduce details
     */
    function toggleDetails() {
        if (displayDetails === "initial") {
            displayDetails = "none";
            buttonImage = "/images/account/expand.png"; // https://img.icons8.com/ios/256/expand-arrow.png
        } else {
            displayDetails = "initial";
            buttonImage = "/images/account/reduce.png"; // https://img.icons8.com/ios/256/collapse-arrow.png
        }
    }

    function cleanup() {
        for (var i = 0; i < products.length; i++) {
            if (products[i].pid) {
                URL.revokeObjectURL(products[i].pid.img);
            }
        }
    }

    onDestroy(cleanup);
</script>

<div class="order">
    {#await loadOrder()}
        <LoadingBar />
    {:then} 
        {#if !error}
            <div class="overview">
                <div class="id">
                    <h2>Bestellnummer: {orderId}</h2>
                </div>
                <div class="timestamp">
                    <h2>Zeitpunkt: {timestamp}</h2>
                </div>
                <div class="price">
                    <h2>Gesamtpreis: {price.toFixed(2)} €</h2>
                </div>
                <div class="button">
                    <button on:click={toggleDetails}><img src={buttonImage} alt="Umschalten der Anzeige an Details"></button>
                </div>
            </div>
            <div class="details" style="display: {displayDetails};">
                <h3>Details</h3>
                <table>
                    <tr>
                        <th class="img">Bild</th>
                        <th>Produktbezeichnung</th>
                        <th>Anzahl</th>
                        <th class="tableprice">Preis</th>
                    </tr>
                    {#each products as position}
                        <tr>
                            <td class="img"><img src={position.pid ? position.pid.img : defaultProductPicture} alt="Bild des Produkts"></td>
                            <td><h3>{position.pid ? position.pid.name : "[Entferntes Produkt]"}</h3></td>
                            <td><h4>{position.amount}x</h4></td>
                            <td class="tableprice"><h4>{position.price.toFixed(2)} €</h4></td>
                        </tr>
                    {/each}
                </table>
            </div>
        {:else}
            <Error error={error} />
        {/if}
    {:catch}
        <Error error="Es ist ein unerwarteter Fehler aufgetreten!" />
    {/await}
</div>

<style>
    * {
        color: #514538;
    }    
    .order {
        border: 1px solid black;
        border-radius: 10px;
        width: 90%;
        height: auto;
        padding: 10px 25px 10px 25px;
        margin: 15px;
    }
    .order:hover {
        box-shadow: 0px 0px 10px 0px gray;
    }
    .overview {
        display: flex;
        justify-content: space-between;
        align-items: center;
    }
    .button img {
        width: 42px;
        height: auto;
    }
    .button img:hover {
        cursor: pointer;
    }
    .img {
        width: 40px;
        height: 40px;
    }
    .tableprice {
        text-align: right;
    }
    table {
        width: 100%;
        border-collapse: collapse;
        border-radius: 10px;
        overflow: hidden;
        margin-top: 10px;
    }
    th {
        text-align: left;
    }
    td, th {
        padding: 5px 15px 5px 15px;
        word-wrap: break-word;
        border-bottom: 2px solid white;
        background-color: #ececec;
    }
    tr img {
        width: 200px;
        border-radius: 10px;
        margin: 10px;
        max-height: 250px;
    }
    button {
        background-color: white;
        border: none;
    }
    h3 {
        font-size: 22px;
    }
    h4 {
        font-size: 20px;
    }
    h3, h4 {
        margin: 0;
    }
    @media (max-width: 1800px) {
        .img {
            min-width: 80px;
        }
    }
    @media (max-width: 1100px) {
        .order {
            width: 80%;
        }
        .overview {
            display: block;
        }
        .img {
            display: none;
        }
    }
</style>