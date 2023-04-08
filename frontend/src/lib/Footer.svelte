<div class="footer">
    <ul>
        <li>Copyright 2023 DogNeeds</li>
        <li><a href="/support">Hilfe</a></li>
        <li><a href="/impressum">Impressum & Datenschutz</a></li>
        <li><a href="/agb">AGBs</a></li>
    </ul>
</div>

<style>
    .footer {
        position: fixed;
        display: flex;
        left: 0;
        bottom: 0;
        width: 100%;
        background-color: #CEA193;
        color: white;
        justify-content: space-evenly;
        align-items: center;
    }
    li {
        display: inline-block;
        margin: 0px 30px;
        font-size: 18px;
        color: #514538;
    }
    a {
        text-decoration: none;
        color: #514538;
    }
    a:hover {
        text-decoration: underline;
    }
    @media (max-width: 640px) {
        .footer {
            position: fixed;
            display: flex;
            left: 0;
            bottom: 0;
            width: 100%;
            background-color: #CEA193;
            color: white;
            justify-content: space-evenly;
        }
        li {
            display: inline-block;
            margin: 0px 10px;
            font-size: 18px;
            color: #514538;
        }
        li:first-child {
            display: none;
        }
    }
</style>