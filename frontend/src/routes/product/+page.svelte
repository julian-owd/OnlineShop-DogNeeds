<script>
    import Header from "$lib/Header.svelte";
    import Footer from "$lib/Footer.svelte";
    import Error from "$lib/Error.svelte";
    import LoadingBar from "$lib/LoadingBar.svelte";
    import Feedback from "$lib/product/Feedback.svelte";
    import { serverAddress, defaultProductPicture, checkAuth } from "$lib/stores.js";
    import { browser } from "$app/environment";
    import { goto } from "$app/navigation";
    import { onMount, onDestroy } from "svelte";
    import { page } from "$app/stores";

    const session = browser ? window.sessionStorage.getItem("session") ?? "" : ""; // session from cookies
    let buttonBackgroundColor = "bisque";

    let productId;
    let name;
    let rating = 0;
    let price;
    let description;
    let img;

    let allowFeedback = false;
    let userRating;
    let userText = "";
    let star1 = "/images/feedback/star-empty.png";
    let star2 = "/images/feedback/star-empty.png";
    let star3 = "/images/feedback/star-empty.png";
    let star4 = "/images/feedback/star-empty.png";
    let star5 = "/images/feedback/star-empty.png";

    let feedbacks = [];

    let error = "Es ist ein Fehler aufgetreten! Bitte begib dich zurück zur Startseite.";

    onMount(async () => {
        productId = $page.url.searchParams.get("p");

        if (browser && productId) {
            if (session) {
                let auth = await checkAuth();
                if (auth) {
                    let request = await fetch("http://" + serverAddress + "/account/addLastViewedProduct", {
                        method: "POST",
                        body: JSON.stringify({
                            session, productId
                        })
                    }).catch(function () {
                        return;
                    });
                    request = await fetch("http://" + serverAddress + "/feedback/check", {
                        method: "POST",
                        body: JSON.stringify({
                            session, productId
                        })
                    });
                    let result = await request.text();
                    if (result === "true") {
                        allowFeedback = true;
                    } else {
                        allowFeedback = false;
                    }
                }
            }
        }
    });

    /**
     * load the details of the product
     */
    async function loadProduct() {
        let request = await fetch("http://" + serverAddress + "/product/request", {
            method: "POST",
            body: productId + ";name;price;description"
        }).catch(() => {
            error = "Es konnte keiner Verbindung zum Server hergestellt werden!";
        });
        let result = await request.text();
        if (result == "error;notfound") {
            error = "Dieses Produkt haben wir leider nicht im Sortiment!";
        } else {
            let productDetails = result.split(";");
            name = productDetails[0];
            price = productDetails[1];
            description = productDetails[2];
            
            request = await fetch("http://" + serverAddress + "/product/img", {
                method: "POST",
                body: productId
            });
            result = await request.blob();
            if (result.size > 0) {
                img = URL.createObjectURL(result);
            } else {
                img = defaultProductPicture;
            }
        }

        request = await fetch("http://" + serverAddress + "/feedback/get", {
            method: "POST",
            body: productId
        });
        result = JSON.parse(await request.text());
        if (result.length == 0) {
            return;
        }
        for (let i = 0; i < result.length; i++) {
            feedbacks[i] = {name: result[i].uid.firstName + " " + result[i].uid.lastName, text: result[i].description, rating: result[i].rating};
        }

        rating = 0;
        for (let i = 0; i < feedbacks.length; i++) {
            rating += feedbacks[i].rating;
        }
        rating = Math.round(rating / feedbacks.length);
    }

    /**
     * add a product to the shopping cart or increase its amount
     */
    async function addToCart() {
        let auth = await checkAuth();
        if (!auth) {
            goto("/account/login?l=2");
            return;
        }
        const request = await fetch("http://" + serverAddress + "/order/addCart", {
            method: "POST",
            body: JSON.stringify({
                session,
                productId
            })
        }).catch(() => {
            error = "Es konnte keiner Verbindung zum Server hergestellt werden!";
            return;
        });
        const result = await request.text();
        if (result !== "success") {
            error = "Das Produkt konnte nicht zum Einkaufswagen hinzugefügt werden!";
            return;
        }

        buttonBackgroundColor = "#74fc89";

        setTimeout(function() {
            buttonBackgroundColor = "bisque";
        }, 250);
    }

    /**
     * submit the feedback of a user
     */
    async function submitFeedback() {
        if (!userRating) {
            return;
        }
        if (userText.length > 100) {
            return;
        }
        console.log(userText.length);

        const request = await fetch("http://" + serverAddress + "/feedback/add", {
            method: "POST",
            body: JSON.stringify({
                session,
                productId,
                userRating,
                userText
            })
        }).catch(() => {
            return;
        });
        const result = await request.text();
        if (result === "true") {
            alert("Dein Feedback wurde gespeichert!");
            await goto("/product?p=" + productId);
            location.reload();
            return;
        } else {
            alert("Dein Feedback konnte nicht gespeichert werden!");
        }
    }

    /**
     * update the filled stars
     * @param i rating of the user
     */
    function updateStars(i) {
        userRating = i;
        star1 = "/images/feedback/star-empty.png";
        star2 = "/images/feedback/star-empty.png";
        star3 = "/images/feedback/star-empty.png";
        star4 = "/images/feedback/star-empty.png";
        star5 = "/images/feedback/star-empty.png";
        if (i >= 1) {
            star1 = "/images/feedback/star-filled.png";
        }
        if (i >= 2) {
            star2 = "/images/feedback/star-filled.png";
        }
        if (i >= 3) {
            star3 = "/images/feedback/star-filled.png";
        }
        if (i >= 4) {
            star4 = "/images/feedback/star-filled.png";
        }
        if (i == 5) {
            star5 = "/images/feedback/star-filled.png";
        }
    }

    function cleanup() {
        if (img) {
            URL.revokeObjectURL(img);
        }
    }
    
    onDestroy(cleanup);
</script>

<svelte:head>
    <title>{name} - DogNeeds</title>
</svelte:head>

<Header />
<div class="main">
    {#if productId}
        {#await loadProduct()}
            <div class="catch">
                <LoadingBar />
            </div>
        {:then} 
            {#if name}
                <div class="product">
                    <div class="image">
                        <img src="{img}" alt="">
                    </div>
                    <div class="information">
                        <div class="details">
                            <div class="title">
                                <h1>{name}<h1>
                            </div>
                            <div class="rating">
                                {#each {length: rating} as _}
                                    <img src="/images/feedback/star-filled.png" alt="Filled star"> <!--https://cdn-icons-png.flaticon.com/512/1828/1828614.png-->
                                {/each}
                                {#each {length: 5 - rating} as _}
                                    <img src="/images/feedback/star-empty.png" alt="Empty star"> <!--https://cdn-icons-png.flaticon.com/512/1828/1828970.png-->
                                {/each}
                                <p>({feedbacks.length})</p>
                            </div>
                            <div class="description">
                                <p>{description}</p>
                            </div>
                            <div class="price">
                                <h2>{parseFloat(price).toFixed(2)} €</h2>
                            </div>
                        </div>
                        <div class="cart">
                            <button on:click={addToCart} style="background-color: {buttonBackgroundColor}">Zum Einkaufswagen hinzufügen</button>
                        </div>
                    </div>
                </div>
                <div class="feedback">
                    <div class="give-feedback" style="display: {allowFeedback ? "flex" : "none"};">
                        <h1>Produkt bewerten</h1>
                        <form action="">
                            <div class="stars">
                                <button type="button" on:click={() => {updateStars(1)}}><img src={star1} alt="1 Stern"></button>
                                <button type="button" on:click={() => {updateStars(2)}}><img src={star2} alt="2 Sterne"></button>
                                <button type="button" on:click={() => {updateStars(3)}}><img src={star3} alt="3 Sterne"></button>
                                <button type="button" on:click={() => {updateStars(4)}}><img src={star4} alt="4 Sterne"></button>
                                <button type="button" on:click={() => {updateStars(5)}}><img src={star5} alt="5 Sterne"></button>
                            </div>
                            <div class="text">
                                <label for="textarea" style="display: {userText.length <= 100 ? "none" : "initial"}">Dein Text darf nicht länger als 100 Zeichen sein!</label>
                                <textarea id="textarea" bind:value={userText} style="{userText.length <= 100 ? "1px solid gray" : "2px solid red"}"></textarea>
                            </div>
                            <div class="submit">
                                <button type="submit" on:click={submitFeedback}>Produkt bewerten</button>
                            </div>
                        </form>
                    </div>
                    {#if feedbacks.length != 0}
                        <h1>Rezensionen</h1>
                        <div class="feedback-items">
                            {#each feedbacks as feedback}
                                <div class="feedback-item">
                                    <svelte:component this={Feedback} {...feedback} />
                                </div>
                            {/each}
                        </div>
                    {/if}
                </div>
            {:else}
                <div class="catch">
                    <Error error={error} />
                </div>
            {/if}
        {:catch}
            <div class="catch">
                <Error error={error} />
            </div>
        {/await}
    {:else}
        <div class="catch">
            <Error error={error} />
        </div>
    {/if}
</div>
<Footer />

<style>
    .main {
        margin-bottom: 5%;
    }
    .catch {
        width: 100%;
        height: 100%;
        display: flex;
        justify-content: center;
        align-items: center;
        padding: 20px;
    }
    .product {
        margin: 1% 1% 1% 1%;
        display: flex;
        flex-direction: row;
        justify-content: left;
        margin-left: 15px;
        width: 90%;
        height: 100%;
    }
    .information {
        margin-left: 2%;
        display: flex;
        flex-direction: column;
        justify-content: space-evenly;
    }
    .feedback {
        margin: 1% 1% 3% 1%;
    }
    .give-feedback {
        flex-direction: column;
        margin-bottom: 20px;
    }
    .stars button {
        background-color: white;
        border: none;
    }
    .stars button:hover {
        background-color: bisque;
        border-radius: 10px;
    }
    .text {
        margin: 10px;
        display: flex;
        flex-direction: column;
    }
    .text label {
        color: #ff6557;
        font-weight: bold;
    }
    .text textarea {
        width: 30%;
        height: 125px;
        padding: 5px;
        font-size: 15px;
        resize: none;
    }
    .submit button {
        background-color: bisque;
    }
    .feedback-items {
        display: flex;
        flex-wrap: wrap;
    }
    .rating {
        height: 25px;
        display: flex;
        align-items: center;
    }
    .rating img {
        margin: 0;
        width: 28px;
        height: auto;
	}
    .rating p {
        margin-left: 10px;
        font-size: 24px;
    }
    .image img {
        width: 500px;
        height: auto;
        max-height: 400px;
        border-radius: 10px;
        border: 1px solid gray;
    }
    h1 {
        font-size: 48px;
        color: #3b332b;
        margin-top: 0;
        margin-bottom: 10px;
    }
    h2 {
        font-size: 32px;
        color: #5c4b3a;
    }
    p {
        font-size: 24px;
        color: #3b332b;
    }
    button {
        font-size: 24px;
        padding: 10px;
        border: 1px solid #3b332b;
        border-radius: 10px;
        color: #3b332b;
        height: fit-content;
    }
    .submit button:hover,
    .cart button:hover {
        cursor: pointer;
        border: 2px solid #3b332b;
    }
    @media (max-width: 640px) {
        .main {
            margin-bottom: 15%;
            margin-left: 0;
        }
        .product {
            margin-top: 5%;
            display: block;
            text-align: center;
            margin-left: 0;
            width: 100%;
        }
        .image img {
            width: 95%;
            height: auto;
        }
        .rating {
            display: flex;
            align-items: center;
            justify-content: center;
        }
        .feedback {
            text-align: center;
            margin-bottom: 15%;
        }
        .give-feedback {
            margin-top: 20px;
        }
        .text textarea {
            width: 95%;
            height: 70px;
            padding: 5px;
            font-size: 15px;
            resize: none;
        }
        .feedback-items {
            display: block;
            justify-content: center;
        }
        .rating img {
            width: 24px;
            height: 24px;
        }
        .rating p {
            margin-left: 10px;
            font-size: 24px;
        }
    }
</style>