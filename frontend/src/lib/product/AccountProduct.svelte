<script>
    import Error from "$lib/Error.svelte";
    import LoadingBar from "$lib/LoadingBar.svelte";
    import { serverAddress, defaultProductPicture } from "$lib/stores.js";
    import { onDestroy } from "svelte";

    export let productId;
    let name = "ERROR";
    let img = "/images/products/not_found.png";
    let rating = 0;
    let numberOfRatings = 0;

    /**
     * load the product
     */
    async function loadProduct() {
        let request = await fetch("http://" + serverAddress + "/product/request", {
            method: "POST",
            body: productId + ";name"
        });
        let result = await request.text();
        if (result == "error;notfound") {
            name = "Fehler beim Laden!";
        } else {
            let productDetails = result.split(";");
            name = productDetails[0];
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
            rating += result[i].rating;
        }
        rating = Math.round(rating / result.length);
        numberOfRatings = result.length;
    }

    function cleanup() {
        if (img) {
            URL.revokeObjectURL(img);
        }
    }

    onDestroy(cleanup);
</script>

<div class="product">
    {#await loadProduct()}
        <LoadingBar />
    {:then} 
        <div class="title">
            <h3>{name}</h3>
        </div>
        <div class="rating">
            {#each {length: rating} as _}
                <img src="images/feedback/star-filled.png" alt="Filled star"> <!--https://cdn-icons-png.flaticon.com/512/1828/1828614.png-->
            {/each}
            {#each {length: 5 - rating} as _}
                <img src="images/feedback/star-empty.png" alt="Empty star"> <!--https://cdn-icons-png.flaticon.com/512/1828/1828970.png-->
            {/each}
            <p>({numberOfRatings})</p>
        </div>
        <div class="image">
            <img src={img} alt={name}>
        </div>
        <div class="link">
            <a href={"/product/?p=" + productId}>Zum Produkt</a>
        </div>
    {:catch}
        <Error error="Das Produkt konnte nicht geladen werden!" />
    {/await}
</div>

<style>
    .product {
        width: fit-content;
        height: fit-content;
        border-radius: 10px;
        padding: 10px 10px 20px 10px;
        box-shadow: 0px 0px 5px 0px #000000;
    }
    .link {
        margin-top: 15px;
    }
    .rating {
        display: flex;
        align-items: center;
    }
    .rating img {
        width: 24px;
        height: 24px;
	}
    .rating p {
        margin-left: 10px;
        font-size: 24px;
    }
    h3 {
		color: #3b332b;
		font-size: 28px;
		font-weight: 60;
        margin-bottom: 0;
        margin-top: 2px;
	}
    .image img {
		width: 240px;
		height: 120px;
		border-radius: 10px;
        border: 1px solid gray;
	}
    a {
        display: block;
        text-decoration: none;
        color: #3b332b;
        padding: 10px;
        border: 2px solid #3b332b;
        border-radius: 10px;
        text-align: center;
    }
    a:hover {
        background-color: blanchedalmond;
    }
    .product:hover {
        box-shadow: 0px 0px 15px 0px #000000;
    }
    @media (max-width: 640px) {
        .product {
            width: 90%;
            text-align: center;
        }
        .rating {
            justify-content: center;
        }
        .rating img {
            width: 28px;
            height: 28px;
        }
        .image img {
            width: 90%;
            height: auto;
        }
        h3 {
            margin-top: 5px;
            font-size: 24px;
        }
        a {
            width: 94%;
        }
    }
</style>