<script>
    import Header from "$lib/Header.svelte";
    import Footer from "$lib/Footer.svelte";
    import CartItem from "$lib/cart/CartItem.svelte";
    import LoadingBar from "$lib/LoadingBar.svelte";
    import Error from "$lib/Error.svelte";
    import { serverAddress, checkAuth } from "$lib/stores.js";
    import { loadScript } from '@paypal/paypal-js';
    import { browser } from "$app/environment";
    import { goto } from "$app/navigation";
    import { onMount } from "svelte";

    const clientID = "{clientID}&components=buttons&currency=EUR&disable-funding=sofort,card,sepa,giropay";
    const session = browser ? window.sessionStorage.getItem("session") ?? "" : ""; // session from cookies

    let error;
    let price = 0;

    let items = [];

    onMount(async () => {
        if (browser) {
            if (session === "") {
                goto("/account/login?l=3");
                return;
            }
        }
        await loadCart();
        updatePrice();
        if (!error) {
            initPayPalButtons();
        }
    });

    async function initPayPalButtons() {
        let paypal;
        try {
            paypal = await loadScript({
                'client-id': clientID
            });
        } catch (error) {
            console.error('failed to load the PayPal JS SDK script', error);
        }

        if (paypal) {
            try {
                await paypal.Buttons({
                    style: {
                        layout: 'vertical',
                        color:  'blue',
                        shape:  'rect',
                        label:  'pay'
                    },
                    createOrder: function (data, actions) {
                        // Set up the transaction
                        return actions.order.create({
                            purchase_units: [
                                {
                                    amount: {
                                        value: price.toFixed(2),
                                    },
                                },
                            ],
                        });
                    },
                    onApprove: async function (data, actions) {
                        // Capture order after payment approved
                        return actions.order.capture().then(function (details) {
                            checkout();
                        });
                    },
                    onError: function (err) {
                        // Log error if something goes wrong during approval
                        error = "Deine Zahlung konnte nicht abgewickelt werden!";
                        console.log('Payment error, ', err);
                    },
                }).render("#paypal-container");
            } catch (error) {
                console.error('Failed to render the buy button', error);
            }
            return paypal;
        }
        return null;
    }

    /**
     * load the cart items
     */
    async function loadCart() {
        let auth = await checkAuth();
        if (!auth) {
            goto("/account/login?l=2");
            return;
        }
        const request = await fetch("http://" + serverAddress + "/order/getCartProducts", {
            method: "POST",
            body: session
        }).catch(function () {
            error = "Es konnte keine Verbindung zum Server hergestellt werden!";
            return;
        });
        const result = JSON.parse(await request.text());
        if (result === "null") {
            error = "Es ist ein unerwarteter Fehler aufgetreten!";
            return;
        }

        // shopping cart is empty
        if (result.length == 0) {
            error = "Dein Einkaufswagen enthält keine Produkte!";
            return;
        }

        for (var i = 0; i < result.length; i++) {
            items[i] = result[i];
        }
    }

    /**
     * update the price of the products
     */
    function updatePrice() {
        price = 0;
        items.forEach(function (item) {
            price += item.price * item.amount;
        });
    }

    /**
     * buy the products in the card
     */
    async function checkout() {
        const request = await fetch("http://" + serverAddress + "/order/checkout", {
            method: "POST",
            body: session
        })

        const result = await request.text();

        if (result.includes("error")) {
            error = "Dein Kauf konnte nicht abgewickelt werden!";
        } else {
            goto("/cart/checkout_success");
        }
    }
</script>

<svelte:head>
    <title>Einkaufswagen - DogNeeds</title>
</svelte:head>

<Header />
<div class="cart">
    <h1>Einkaufswagen ({items.length})</h1>
    {#await loadCart()}
        <LoadingBar />
    {:then}
        <div class="box">
            {#if !error}
                <div class="items">
                    {#each items as item}
                        <div class="item" on:keydown={updatePrice}>
                            <CartItem productId={item.pid} name={item.name} price={item.price} amount={item.amount} />
                        </div>
                    {/each}
                </div>
                <div class="overview">
                    <h1>Zusammenfassung</h1>
                    <p>Summe: {price.toFixed(2)}€</p>
                    <div id="paypal-container" />
                </div>
            {:else}
                <Error error={error} />
            {/if}
        </div>
    {/await}
</div>
<Footer />

<style>
    * {
        color: #514538;
    }
    .cart {
        width: 95%;
        margin: 1% 1% 3% 1%;
    }
    .box {
        display: flex;
        justify-content: space-between;
    }
    .items {
        width: 70%;
    }
    .item {
        margin-bottom: 15px;
    }
    .overview {
        position: sticky;
        top: 15%;
        box-shadow: 0px 0px 5px 0px #000000;
        border-radius: 10px;
        padding: 10px;
        height: fit-content;
    }
    .overview:hover {
        box-shadow: 0px 0px 10px 0px #514538;
    }
    .overview h1 {
        margin-top: 0;
    }
    h1 {
        color: #3b332b;
        font-size: 32px;
    }
    p {
        color: #3b332b;
        font-size: 20px;
    }
    @media (max-width: 1060px) {
        .box {
            display: grid;
        }
        .items {
            width: 95%;
        }
    }
</style>