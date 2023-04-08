<script>
    import { serverAddress, defaultProductPicture } from "$lib/stores.js";
    import { browser } from "$app/environment";
    import { onMount, onDestroy } from "svelte";

    export let productId;
    export let name;
    let img;
    export let price;
    export let amount = 1;

    const session = browser ? window.sessionStorage.getItem("session") ?? "" : ""; // session from cookies

    onMount(async () => {
        let request = await fetch("http://" + serverAddress + "/product/img", {
            method: "POST",
            body: productId
        });
        let result = await request.blob();
        if (result.size > 0) {
            img = URL.createObjectURL(result);
        } else {
            img = defaultProductPicture;
        }
    });

    /**
     * increase amount
     */
    async function increaseAmount() {
        await fetch("http://" + serverAddress + "/order/addCart", {
            method: "POST",
            body: JSON.stringify({
                session,
                productId
            })
        });

        location.reload();
    }

    /**
     * decrease amount
     */
    async function decreaseAmount() {
        if (amount > 1) {
            await fetch("http://" + serverAddress + "/order/decreaseCart", {
                method: "POST",
                body: JSON.stringify({
                    session,
                    productId
                })
            });

            location.reload();
        }
    }

    /**
     * remove product from cart
     */
    async function removeItem() {
        await fetch("http://" + serverAddress + "/order/deleteCart", {
            method: "POST",
            body: JSON.stringify({
                session,
                productId
            })
        });

        location.reload();
    }

    function cleanup() {
        if (img) {
            URL.revokeObjectURL(img);
        }
    }

    onDestroy(cleanup);
</script>

<div class="item">
    <div class="details">
        <h2>{name}</h2>
        <img src="{img}" alt="">
    </div>
    <div class="config">
        <div class="amount">
            <button on:click={decreaseAmount}><img src="/images/cart/minus.png" alt=""></button> <!--https://img.icons8.com/ios/256/minus.png-->
            <p>{amount}</p>
            <button on:click={increaseAmount}><img src="/images/cart/plus.png" alt=""></button> <!--https://img.icons8.com/ios/256/add.png-->
        </div>
        <div class="price">
            <h3>{(price * amount).toFixed(2)}€</h3>
        </div>
        <div class="remove">
            <button on:click={removeItem}><img src="/images/cart/delete.png" alt=""></button> <!--https://img.icons8.com/ios/256/circled-x.png-->
        </div>
    </div>
</div>

<style>
    .item {
        width: 640px;
        display: flex;
        box-shadow: 0px 0px 5px 0px #000000;
        border-radius: 10px;
        padding: 10px;
        justify-content: space-between;
        align-items: center;
    }
    .item:hover {
        box-shadow: 0px 0px 10px 0px #000000;
    }
    .details img {
        width: 200px;
        height: auto;
        border-radius: 10px;
        border: 1px solid gray;
    }
    .config img {
        width: 48px;
        height: auto;
    }
    .config img:hover {
        cursor: pointer;
        border-radius: 60px;
        background-color: antiquewhite;
    }
    .config {
        display: flex;
        justify-content: space-around;
        align-items: center;
    }
    .amount {
        display: flex;
        align-items: center;
    }
    h2 {
        margin: 5px 0 5px 0;
        font-size: 28px;
        color: #3b332b;
    }
    h3 {
        margin: 0;
        font-size: 24px;
        color: #3b332b;
    }
    p {
        width: 20%;
        color: #3b332b;
        font-size: 24px;
        padding: 0 5px 0 5px;
    }
    button {
        background-color: white;
        border: none;
    }
    @media (max-width: 640px) {
        .item {
            width: 100%;
        }
        .details img {
            width: 180px;
        }
        .config img {
            width: 32px;
        }
        .config {
            margin-left: 15px;
            display: block;
        }
        .remove {
            margin-top: 10px;
        }
        h3 {
            font-size: 20px;
        }
        p {
            width: 30px;
        }
    }
</style>