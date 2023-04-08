<script>
    export let name;
    export let img;
    export let url;
</script>

<div class="product">
    <div class="title">
        <h3>{name}</h3>
    </div>
    <div class="image">
        <img src={img} alt={name}>
    </div>
    <div class="link">
        <a href={url}>Zum Produkt</a>
    </div>
</div>

<style>
    .product {
        width: 350px;
        height: fit-content;
        border-radius: 10px;
        padding: 10px 10px 20px 10px;
        box-shadow: 0px 0px 5px 0px #000000;
    }
    .link {
        margin-top: 15px;
    }
    h3 {
		color: #3b332b;
		font-size: 32px;
		font-weight: 60;
        margin-bottom: 5px;
        margin-top: 2px;
	}
    img {
		width: 320px;
		height: 200px;
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
        box-shadow: 0px 0px 10px 0px #000000;
    }
    @media (max-width: 640px) {
        .product {
            width: 100%;
        }
        img {
            width: 90%;
            height: auto;
        }
        h3 {
            font-size: 4vw;
        }
        a {
            width: 50%;
            margin-left: 20%;
        }
    }
</style>