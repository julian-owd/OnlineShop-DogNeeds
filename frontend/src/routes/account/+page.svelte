<script>
    import Header from "$lib/Header.svelte"
    import Footer from "$lib/Footer.svelte";
    import AccountProduct from "$lib/product/AccountProduct.svelte";
    import LoadingBar from "$lib/LoadingBar.svelte";
    import Error from "$lib/Error.svelte";
    import { serverAddress, checkAuth } from "$lib/stores.js";
    import { browser } from "$app/environment";
    import { goto } from "$app/navigation";
    import { onMount } from "svelte";
    
    let session = browser ? window.sessionStorage.getItem("session") ?? "" : ""; // session from cookies
    let checkedAuth = 0; // check once for connection

    let name = "";
    let lastViewedProducts = [];

    /**
     * called on side load
    */
    onMount(async () => {
        if (browser) {
            if (session === "") {
                goto("/account/login");
                return;
            }
        }
        if (checkedAuth == 0) {
            checkedAuth = 1;
            let auth = await checkAuth();
            if (!auth) {
                logout(2);
            }
        }
        await getName();
    });

    /**
     * requests the name of the current user
     */
    async function getName() {
        const response = await fetch("http://" + serverAddress + "/account/request", {
            method: "POST",
            body: session + ";firstName;lastName"
        });
        const result = await response.text();
        const data = result.split(";");
        name = data[0] + " " + data[1];
    }

    /**
     * load the last viewed products
     */
    async function loadLastViewedProducts() {
        if (!session) {
            return;
        }
        let request = await fetch("http://" + serverAddress + "/account/getLastViewedProducts", {
            method: "POST",
            body: session
        });
        let result = JSON.parse(await request.text());
        for (let i = 0; i < result.length; i++) {
            lastViewedProducts[i] = {id: result[i]};
        }
    }

    /**
     * destroys the current session
     * @param id 1: Normal logout 2: Unable to retrieve session
     */
    async function logout(id) {
        let logoutId; 
        if (id == 2) {
            logoutId = 2;
        } else {
            logoutId = 1;
        }
        let url = "";
        if (logoutId == 1) {
            url = "/account/login?l=1"; // normal logout
        } else if (logoutId == 2) {
            url = "/account/login?l=2"; // error logout
        }

        // check authentification on server; true if success
        await fetch("http://" + serverAddress + "/account/logout", {
            method: "POST",
            body: session
        }).catch(function() {
            if (browser) {
                sessionStorage.removeItem('session');
            }
            session = "";
            goto(url);
        });

        // delete cookies
        if (browser) {
            sessionStorage.removeItem('session');
        }
        session = "";

        // leave site
        goto(url);
    }
</script>

<svelte:head>
    <title>Account - DogNeeds</title>
</svelte:head>

<Header />
<div await class="account">
    {#if session !== ""}
        <h1>Hallo {name}</h1>
        <div class="actions">
            <button on:click={() => goto("/account/orders")}>Meine Bestellungen</button>
            <button on:click={() => goto("/account/edit")}>Mein Profil bearbeiten</button>
            <button on:click={() => goto("/support")}>Zum Support</button>
            <button on:click={logout} value="1">Logout</button>
        </div>
        <div class="last-viewed">
            <h2>Zuletzt angesehene Artikel</h2>
            {#await loadLastViewedProducts()}
                <LoadingBar />
            {:then}
                {#if lastViewedProducts.length > 0}
                    <div class="last-viewed-items">
                        {#each lastViewedProducts as product}
                            <div style="margin: 15px;">
                                <svelte:component this={AccountProduct} productId={product.id} />
                            </div>
                        {/each}
                    </div>
                {:else}
                    <Error error={"Du hast dir bisher noch keine Artikel angesehen!"} />
                {/if}
            {:catch}
                <Error error={"Deine zuletzt angesehenen Artikel konnten nicht geladen werden!"} />
            {/await}

        </div>
        
    {:else}
        <Error error={"Du konntest nicht eingeloggt werden!"} />
        <a href="/account/login">Einloggen</a>
    {/if}
</div>
<Footer />

<style>
    .account {
        margin-left: 5%;
        margin-bottom: 4%;
    }
    h1 {
        font-size: 2.5vw;
        color: #514538;
    }
    h2 {
        font-size: 2vw;
        color: #514538;
    }
    .actions {
        display: flex;
    }
    button {
        margin: 10px;
        padding: 0 15px 0 15px;
        background-color: #fad8af;
        border-radius: 10px;
        font-size: 1.5vw;
        height: 80px;
        color: #3b332b;
    }
    .actions button:hover {
        background-color: #FFC075;
        cursor: pointer;
    }
    .last-viewed-items {
        display: flex;
        justify-content: left;
        padding-right: 50px;
        overflow: auto; /* make div scrollable */
    }
    @media (max-width: 640px) {
        h1 {
            font-size: 32px;
        }
        h2 {
            font-size: 24px;
        }
        .actions {
            display: grid;
        }
        .actions button {
            font-size: 24px;
        }
        .account {
            margin-left: 0;
            margin-bottom: 20%;
        }
    }
</style>