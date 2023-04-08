<script>
    export let name = "Anonym";
    export let text = "Keine Beschreibung gegeben"; // 100 Zeichen Limit
    export let rating = 5;
    export let remainingStars = 5-rating;
</script>

<div class="feedback">
    <div class="name">
        <h3>{name}</h3>
    </div>
    <div class="rating">
        {#each {length: rating} as _}
            <img src="images/feedback/star-filled.png" alt="Filled star"> <!--https://cdn-icons-png.flaticon.com/512/1828/1828614.png-->
        {/each}
        {#each {length: remainingStars} as _}
            <img src="images/feedback/star-empty.png" alt="Empty star"> <!--https://cdn-icons-png.flaticon.com/512/1828/1828970.png-->
        {/each}
    </div>
    <div class="text">
        <p>{text}</p>
    </div>
</div>

<style>
    .feedback {
        width: 280px;
        height: fit-content;
        padding: 10px 10px 20px 10px;
        text-align: center;
    }
    h3 {
        color: #3b332b;
		font-size: 24px;
		font-weight: bold;
        margin-bottom: 5px;
        margin-top: 2px;
        word-wrap: break-word;
    }
    p {
		color: #3b332b;
		font-size: 20px;
		font-weight: 60;
        margin-bottom: 5px;
        margin-top: 2px;
        word-wrap: break-word;
	}
    img {
        margin: 5px;
	}
    @media (max-width: 640px) {
        .feedback {
            width: 90%;
            height: fit-content;
            padding: 10px 10px 20px 10px;
            text-align: center;
        }
        img {
            width: 24px;
            height: auto;
        }
    }
</style>