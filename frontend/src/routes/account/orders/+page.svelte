<script>
    import Header from "$lib/Header.svelte";
    import Footer from "$lib/Footer.svelte";
    import LoadingBar from "$lib/LoadingBar.svelte";
    import Error from "$lib/Error.svelte";
    import { serverAddress, checkAuth } from "$lib/stores.js";
    import { browser } from "$app/environment";
    import { goto } from "$app/navigation";
    import OrderItem from "$lib/account/order/OrderItem.svelte";

    const session = browser ? window.sessionStorage.getItem("session") ?? "" : ""; // session from cookies

    let error = "";
    let orderIds = [];

    /**
     * load the orders of a user
     */
    async function loadOrders() {
        let auth = await checkAuth();
        if (!auth) {
            goto("/account/login?l=2");
            return;
        }

        const response = await fetch("http://" + serverAddress + "/order/getOrders", {
            method: "POST",
            body: JSON.stringify({session})
        }).catch(function () {
            error = "Deine Bestellungen konnten nicht geladen werden!"
            return;
        });
        const result = JSON.parse(await response.text());
        if (result.includes("error")) {
            error = "Deine Bestellungen konnten nicht geladen werden!";
            return;
        }
        if (result.length == 0) {
            error = "Es sind noch keine Bestellungen für deinen Account vorhanden!";
            return;
        }
        orderIds = result;
    }
</script>

<svelte:head>
    <title>Meine Bestellungen - DogNeeds</title>
</svelte:head>

<Header />
<div class="orders">
    <a href="/account">Zurück zur Übersicht</a>
    {#await loadOrders()}
        <LoadingBar />
    {:then}
        {#if !error}
            <h1>Deine Bestellungen</h1>
            <div class="order-items">
                {#each orderIds as orderId}
                    <OrderItem orderId={orderId} />
                {/each}
            </div>
        {:else}
            <Error error={error} />
        {/if}
    {:catch}
        <Error error="Deine Bestellungen konnten nicht geladen werden!" />
    {/await}
</div>
<Footer />

<style>
    * {
        color: #514538;
    }
    .orders {
        display: flex;
        flex-direction: column;
        margin-left: 5%;
        margin-bottom: 15%;
        justify-content: center;
    }
    a {
        font-size: 38px;
        text-decoration: none;
        font-weight: bold;
        margin-top: 0.67em;
        margin-bottom: 0.67em;
        text-align: center;
    }
    h1 {
        font-size: 42px;
    }
    
    @media (max-width: 640px) {
        .orders {
            margin-left: 0;
        }
    }
</style>