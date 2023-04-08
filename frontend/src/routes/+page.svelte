<script>
	import Header from "$lib/Header.svelte"
	import Footer from "$lib/Footer.svelte";
	import HomeProduct from "$lib/product/HomeProduct.svelte";
	import CustomerPromise from "$lib/home/CustomerPromise.svelte";
	import Feedback from "$lib/product/Feedback.svelte";
	import LoadingBar from "$lib/LoadingBar.svelte";
	import { serverAddress, defaultProductPicture } from "$lib/stores.js";
	import { onDestroy } from "svelte";

	let productComponent = HomeProduct;
	let promiseComponent = CustomerPromise;
	let feedbackComponent = Feedback;
	let productProps = [];
	let promiseProps = [
		{icon: "images/promises/truck.png", alt: "LKW mit Paket", text: "Schneller Versand"}, // https://img.icons8.com/wired/512/checked-truck.png
		{icon: "images/promises/best-seller.png", alt: "1. Platz Abzeichen", text: "Hochwertige Produkte"}, // https://img.icons8.com/ios/512/sparkling-diamond.png
		{icon: "images/promises/secure-payments.png", alt: "Schloss", text: "Sichere Bezahlung"}, // https://img.icons8.com/ios/512/keyhole-shield.png
		{icon: "images/promises/customer-support.png", alt: "Person mit Headset", text: "Schneller Kundenservice"}, // https://img.icons8.com/ios/512/customer-support.png		
	];
	let feedbackProps = [];

	async function loadFeedback() {
		let request = await fetch("http://" + serverAddress + "/feedback/getAll", {
			method: "POST"
		});
		let result = JSON.parse(await request.text());
		if (result.length == 0) {
			return;
		}
		for (let i = 0; i < result.length; i++) {
			if (result[i].description) {
				feedbackProps[i] = {name: result[i].uid.firstName + " " + result[i].uid.lastName, text: result[i].description, rating: result[i].rating};
			} else {
				feedbackProps[i] = {name: result[i].uid.firstName + " " + result[i].uid.lastName, rating: result[i].rating};
			}
		}
	}

	async function loadProducts() {
		let request = await fetch("http://" + serverAddress + "/product/getBest", {
			method: "POST"
		});
		let result = JSON.parse(await request.text());
		if (result.length == 0) {
			return;
		}
		for (var i = 0; i < result.length; i++) {
			let imageRequest = await fetch("http://" + serverAddress + "/product/img", {
                method: "POST",
                body: result[i].pid
            });
            let imageBlob = await imageRequest.blob();
			var img;
            if (imageBlob.size > 0) {
                img = URL.createObjectURL(imageBlob);
            } else {
                img = defaultProductPicture;
            }
			productProps[i] = {name: result[i].name, img: img, url: "/product?p=" + result[i].pid}
		}
	}
	
	function cleanup() {
		for (var i = 0; i < productProps.length; i++) {
			URL.revokeObjectURL(productProps[i].img);
		}
	}

	onDestroy(cleanup);
</script>

<svelte:head>
	<title>DogNeeds</title>
</svelte:head>

<Header />
<main>
	<div class="text">
		<h1>Für die besten Freunde des Menschen</h1>
		<h2>Alles was Ihren Hund glücklich macht!</h2>
		<p>
			DogNeeds hat es sich zum Auftrag gemacht, Ihren Vierbeinern hochwertige Produkte bereitzustellen!
			<br>
			Denn ein glücklicher Hund, macht einen glücklichen Menschen.
		</p>
	</div>

	<div class="dogs">
		<img src="images/dogs/happy_dog1.jpg" alt="Happy black dog"> <!--https://pxhere.com/de/photo/1193143-->
		<img src="images/dogs/happy_dog2.jpg" alt="Happy white dog"> <!--https://pixnio.com/de/tiere/hunde/weisser-hund-augen-gesicht-pelz-gras-gluecklich-haustier-->
	</div>

	<div class="product">
		{#await loadProducts()}
			<LoadingBar />
		{:then}
			{#if productProps.length > 0}
				<h2>Bestseller</h2>
				<div class="productitems">
					{#each productProps as product}
						<div style="margin: 20px;">
							<svelte:component this={productComponent} {...product} />
						</div>
					{/each}
				</div>
			{/if}
		{/await}
	</div>

	<div class="customerpromise">
		<h2>Kundenversprechen</h2>
		<div class="promises">
			{#each promiseProps as promise}
				<div style="margin: 20px;">
					<svelte:component this={promiseComponent} {...promise} />
				</div>
			{/each}
		</div>
	</div>
	
	<div class="feedback">
		{#await loadFeedback()}
			<LoadingBar />
		{:then}
			{#if feedbackProps.length > 0}
				<h2>Rezensionen</h2>
				<div class="feedbacks" id="feedbacksContainer">
					{#each feedbackProps as feedback}
						<div class="feedback-item">
							<svelte:component this={feedbackComponent} {...feedback} />
						</div>
					{/each}
				</div>
			{/if}
		{/await}
	</div>
</main>
<Footer />

<style>
	main {
		text-align: center;
		margin-left: 10%;
		margin-right: 10%;
		margin-bottom: 4%;
	}
	h1 {
		color: #514538;
		font-size: 48px;
		font-weight: bold;
	}
	h2 {
		color: #514538;
		font-size: 2.5vw;
		font-weight: 80;
		margin-bottom: 5px;
	}
	p {
		font-size: 1.5vw;
		color: #807159;
	}
	.product, .customerpromise, .feedback {
		width: 100%;
	}
	.productitems, .promises {
		display: flex;
		justify-content: center;
		margin-bottom: 10px;
	}
	.feedbacks {
		display: flex;
		flex-direction: row;
		flex-wrap: wrap;
		justify-content: center;
		width: 100%;
		height: fit-content;
		margin-bottom: 20px;
	}
	.feedback-item {
		margin: 10px;
	}
	.dogs img {
		width: 500px;
		height: auto;
		margin: 15px;
		border-radius: 10px;
	}
	@media (max-width: 640px) {
		main {
			max-width: 100%;
			margin-bottom: 15%;
		}
		.text {
			width: 100%;
		}
		h1 {
			font-size: 6vw;
		}
		h2 {
			font-size: 5.5vw;
		}
		p {
			font-size: 4vw;
		}
		.dogs img {
			width: 100%;
			height: auto;
			margin: 10px;
			border-radius: 10px;
		}
		.productitems, .promises {
			display: grid;
			justify-content: center;
			margin-bottom: 10px;
		}
		.feedbacks {
			display: grid;
			margin-bottom: 30px;
		}
		.feedback-item {
			margin: 0;
		}
	}
</style>