<script>
    import { serverAddress, defaultProductPicture } from "$lib/stores.js";
    import { onMount } from "svelte";
    import { onDestroy } from "svelte";

    export let productId;
    export let name;
    let img;
    export let url;
    export let price;
    export let rating = 0;
    export let numberOfRatings = 0;

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
    });

    function cleanup() {
        if (img) {
            URL.revokeObjectURL(img);
        }
    }

    onDestroy(cleanup);
</script>

<div class="product">
    <div class="image">
        <img src={img} alt={name}>
    </div>
    <div class="box">
        <div class="details">
            <div class="title">
                <h3>{name}</h3>
            </div>
            <div class="rating">
                {#each {length: rating} as _}
                    <img src="/images/feedback/star-filled.png" alt="Filled star"> <!--https://cdn-icons-png.flaticon.com/512/1828/1828614.png-->
                {/each}
                {#each {length: 5 - rating} as _}
                    <img src="/images/feedback/star-empty.png" alt="Empty star"> <!--https://cdn-icons-png.flaticon.com/512/1828/1828970.png-->
                {/each}
                <p>({numberOfRatings})</p>
            </div>
            <div class="price">
                <h4>{price} €</h4>
            </div>
        </div>
        <div class="link">
            <a href={url}>Zum Produkt</a>
        </div>
    </div>
    
</div>

<style>
    .product {
        width: 600px;
        height: 200px;
        border-radius: 10px;
        padding: 10px 10px 10px 10px;
        box-shadow: 0px 0px 5px 0px #000000;
        display: flex;
    }
    .product:hover {
        box-shadow: 0px 0px 10px 0px #514538;
    }
    .box {
        display: flex;
        flex-direction: column;
        justify-content: space-between;
        margin-left: 15px;
        width: 100%;
        height: 100%;
    }
    .image img {
        width: 300px;
        height: 200px;
        border-radius: 10px;
        border: 1px solid gray;
    }
    h3 {
		color: #3b332b;
		font-size: 28px;
		font-weight: 400;
        margin-bottom: 5px;
        margin-top: 2px;
	}
    .rating {
        height: 25px;
        display: flex;
        align-items: center;
    }
    .rating img {
        width: 22px;
        height: auto;
	}
    .rating p {
        margin-top: 25px;
        margin-left: 10px;
        font-size: 22px;
    }
    .price {
        margin: 10px 0 0 0;
    }
    .price h4 {
		color: #3b332b;
		font-size: 24px;
		font-weight: 200;
        margin: 0;
	}
    .link a:hover {
        background-color: blanchedalmond;
    }
    a {
        margin-top: auto;
        display: block;
        text-decoration: none;
        color: #3b332b;
        padding: 10px;
        border: 2px solid #3b332b;
        border-radius: 10px;
        text-align: center;
        width: 90%;
    }    
    @media (max-width: 1400px) {
        .product {
            width: 90%;
            height: fit-content;
            display: flex;
            flex-direction: column;
        }
        .box {
            width: 90%;
        }
        .link {
            margin-top: 10px;
        }
        .rating {
            display: flex;
            align-items: center;
            justify-content: left;
        }
        .rating img {
            width: 24px;
            height: 24px;
        }
        h3 {
            font-weight: 60;
            margin-bottom: 0;
            margin-top: 5px;
        }
        .image img {
            width: 100%;
            height: auto;
            max-width: 1400px;
            max-height: 500px;
        }
    }
</style>