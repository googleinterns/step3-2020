/**
 * Add the searched organizations by keyword
 */
function searchOrgs(page, key) {
  var keyword = key;
  if (key === undefined) {
    keyword = document.getElementById('keyword').value;
  }
  closeSearch();
  removeChildren('existing-organizations');
  const pageElement = document.getElementById('current-page');
  // -1 for prev page and -2 for next page
  if (page === -1) {
    if (pageElement.innerText > 0) {
      pageElement.innerText--;
    }
  } else if (page === -2) {
    pageElement.innerText++;
  } else {
    pageElement.innerText = page;
  }
  const qs = '/sql?' + updateQueryString('keyword', keyword) + '&' + updateQueryString('page', pageElement.innerText);
  addOrgs(qs, 1, keyword);
}

function addPagination(count, keyword) {
  const activePage = document.getElementById('current-page').innerText;
  // TODO: highlight the current active page
  document.getElementById('pagination').style.display = 'inline-block';
  if (count !== undefined) {
    removeChildren('pagination-list');

    if (count === 0) {
      return;
    }

    const paginationElement = document.getElementById('pagination-list');
    const prevPage = document.createElement('li');
    prevPage.className = 'page_num';
    prevPage.onclick = function() { searchOrgs(-1, keyword); };
    prevPage.innerText = 'prev';
    paginationElement.appendChild(prevPage);

    const pages = count / 10 + 1;
    for (var i = 1; i < pages; i++) {
      var pageElement = document.createElement('li');
      const index = i - 1;
      pageElement.className = 'page_num';
      pageElement.onclick = function() { searchOrgs(index, keyword); };
      pageElement.innerText = i;
      paginationElement.appendChild(pageElement);
    }

    const nextPage = document.createElement('li');
    nextPage.className = 'page_num';
    nextPage.onclick = function() { searchOrgs(-2, keyword); };
    nextPage.innerText = 'next';
    paginationElement.appendChild(nextPage);
  }
}

function addOrgs(qs, results, keyword) {
  fetch(qs).then(response => response.json()).then(text => {
    const orgsContainer = document.getElementById('existing-organizations');
    if (results) {
      const count = text[0];
      addPagination(count, keyword);
      if (keyword !== undefined) {
        addTitle(keyword, count);
      }
      const data = text[1];
      data.forEach(entry => {
        orgsContainer.appendChild(getOrgAsHtmlDescription(entry, results));
      });
    } else {
      text.forEach(entry => {
        orgsContainer.appendChild(getOrgAsHtmlDescription(entry, results));
      });
    }
  });
}
 
function removeChildren(id) {
  // remove previously displayed similar organizations
  var existingChildren = document.getElementById(id);
  while (existingChildren.firstChild) {
    existingChildren.removeChild(existingChildren.firstChild);
  }
}
 
/**
 * Returns an updated URL search param
 */
function updateQueryString(key, value) {
  var searchParams = new URLSearchParams();
  searchParams.append(key, value);
  return searchParams;
}
 
/**
 * Creates list element from org
 */
function getOrgAsHtmlDescription(org, results) {
  const orgElement = document.createElement('div');
  orgElement.setAttribute("class", "mdc-card");
  // org name
  // TODO: stop event propagation after user clicks on the href link on the tag
  const nameElement = document.createElement('a');
  nameElement.setAttribute("id", "org-name");
  nameElement.setAttribute('href', 'https://' + org.link);
  nameElement.setAttribute('target', '_blank');
  nameElement.innerText = org.name;
  nameElement.onclick = function() { event.stopPropagation(); };
  orgElement.appendChild(nameElement);
  
  // about
  const aboutElement = document.createElement('p');
  aboutElement.setAttribute("id", "about");
  aboutElement.innerText = org.about;
  orgElement.appendChild(aboutElement);

  // like this in chip format
  const neighborElement = document.createElement('p');
  neighborElement.setAttribute('id', 'like-this');
  neighborElement.innerText = 'Like this: ';
  const org1 = document.createElement("span");
  org1.setAttribute("class", "mdc-chip");
  neighborElement.appendChild(org1);
  const org2 = document.createElement("span");
  org2.setAttribute("class", "mdc-chip");
  neighborElement.appendChild(org2);
  const org3 = document.createElement("span");
  org3.setAttribute("class", "mdc-chip");
  neighborElement.appendChild(org3);
  const org4 = document.createElement("span");
  org4.setAttribute("class", "mdc-chip");
  neighborElement.appendChild(org4);

  // get neighboring org id number from CloudSQLManager.java
  org1.appendChild(getNeighborElement(org.neighbor1_id, org.neighbor1));
  org2.appendChild(getNeighborElement(org.neighbor2_id, org.neighbor2));
  org3.appendChild(getNeighborElement(org.neighbor3_id, org.neighbor3));
  org4.appendChild(getNeighborElement(org.neighbor4_id, org.neighbor4));
  orgElement.appendChild(neighborElement);

  const ratingElement = document.createElement('div');
  ratingElement.setAttribute('class', 'rating-element');
  ratingElement.innerText = "Do you like this organization? ";
  const upvoteElement = document.createElement('span');
  upvoteElement.setAttribute('class', 'material-icons rating')
  upvoteElement.innerText = 'thumb_up';

  upvoteElement.onclick = function() { redirectRating(1, org.id); }
  ratingElement.appendChild(upvoteElement);
  const downvoteElement = document.createElement('span');
  downvoteElement.setAttribute('class', 'material-icons rating')
  downvoteElement.innerText = 'thumb_down';
  downvoteElement.onclick = function() { redirectRating(0, org.id); }
  ratingElement.appendChild(downvoteElement);
  orgElement.appendChild(ratingElement);

  // make the whole list element clickable and take user to organization.html pasing id as parameter
  if (results) {
    orgElement.onclick = function() { redirectId(org.id); };
  }
  return orgElement;
}

function getNeighborElement(neighborId, neighborName) {
  const element = document.createElement('a');
  const qs = updateQueryString('id', neighborId);
  const redirect = '/organization.html?' + qs;
  element.setAttribute('href', redirect);
  element.innerText = neighborName;
  return element;
}

/**
 * Redirects user to organization.html
 */
function redirectId(id) {
  const qs = updateQueryString('id', id);
  const redirect = '/organization.html?' + qs;
  window.location = redirect;
}

function redirectRating(up, id) {
  var rating = 'rating=good';
  if (!up) {
    rating = 'rating=bad';
  }
  const params = rating + '&id=' + id;
  fetch('/rating?' + params, {method: 'POST'}).then(response => response.text()).then(message => {
    alert(message);
  });
  event.stopPropagation();
}

function indexPageSearch() {
  const keywordInput = document.getElementById('keyword');
  const keyword = keywordInput.value;
  if (keyword !== '') {
    redirectKeyword(keyword);
  } else {
    alert('Please enter a keyword to search');
  }
}

function searchByKeyword() {
  var keyword = document.getElementById('keyword').value;
  if (keyword === '') {
    alert('Please enter a keyword to search');
  } else {
    searchOrgs(0, keyword);
  }
  closeSearch();
}

function redirectKeyword(keyword) {
  const qs = updateQueryString('keyword', keyword);
  const redirect = '/results.html?' + qs;
  window.location = redirect;
}
 
function addCloseListener() {
  document.addEventListener('click', event => {
    if (event.target.id === 'myOverlay') {
      closeSearch();
    }
  });
}

function addListenerResults() {
  const inputBox = document.getElementById('keyword');
  inputBox.addEventListener('keyup', function(event) {
    if (event.key === 'Enter') {
      searchByKeyword();
    }
  });
}

function addListener() {
  const inputBox = document.getElementById('keyword');
  inputBox.addEventListener('keyup', function(event) {
      if (event.key === 'Enter') {
        detailSearch();
      }
  });
}
 
function addTitle(keyword, count) {
  const element = document.getElementById('results-title');
  if (count > 0) {
    element.innerText = 'Results for ' + keyword + ': ';
  } else {
    element.innerText = 'No results found for ' + keyword;
  }
}

/**
 * Send request to server for classifications
 */
function getClassifications() {
  const qs = '/data';
  const rootNavMenu = createNewNavDrawer();
  fetch(qs).then(response => response.json()).then(tree => {
    tree.roots.forEach(root=> {
      rootNavMenu.firstChild.firstChild.appendChild(addToClassTree(tree, root, root));
    });
    rootNavMenu.setAttribute('class', 'mdc-drawer');
    rootNavMenu.firstChild.firstChild.firstChild.setAttribute('aria-current', 'page');
    document.getElementById('roots').appendChild(rootNavMenu);
  }); 
}

/**
 * Creater Container for new layer of nav tree
 */
function createNewNavDrawer() {
  const asideElem = document.createElement('aside');
  asideElem.setAttribute('class', 'mdc-drawer--dismissible');
  const divElem = document.createElement('div');
  divElem.setAttribute('class', 'mdc-drawer__content');
  const navElem = document.createElement('nav');
  navElem.setAttribute('class', 'mdc-list');
  const divider = document.createElement('li');
  divider.setAttribute('role', 'separator');
  divider.setAttribute('class', 'mdc-list-divider');
  navElem.appendChild(divider);
  divElem.appendChild(navElem);
  asideElem.appendChild(divElem);
  return asideElem;
}

/**
 * Create a single node for distinct category
 */
function createNavItem(parent, classPath) {
  const parentElem = document.createElement('a');
  parentElem.setAttribute('value', classPath);
  parentElem.href = '#';
  parentElem.setAttribute('class', 'mdc-list-item mdc-list-item');
  const span1 = document.createElement('span');
  span1.setAttribute('class', 'mdc-list-item__ripple');
  const span2 = document.createElement('span');
  span2.setAttribute('class', 'mdc-list-item__text');
  span2.innerText = parent;
  const b = document.createElement('button');
  b.setAttribute('class', 'caret');
  const icon = document.createElement('i');
  icon.setAttribute('class', 'material-icons mdc-list-item__graphic');
  icon.innerText = 'chevron_right';
  b.appendChild(icon);
  parentElem.appendChild(span1);
  parentElem.appendChild(b);
  parentElem.appendChild(span2);
  parentElem.style.position = 'relative';
  return parentElem;
}

/**
 * Recursive creation of tree from single root category
 */
function addToClassTree(tree, parent, classPath) {
  const pathElem = createNavItem(parent, classPath);  

  if (tree[parent].length !== 0) {
    const nested = createNewNavDrawer();
    const layer = nested.firstChild.firstChild; 
    layer.style.marginLeft = pathElem.offsetLeft + 30 + 'px';
    const divider = layer.firstChild;
    tree[parent].forEach(child => {
      if (tree[child].length !== 0) {
        layer.appendChild(addToClassTree(tree, child, classPath + '/' + child));
      } else {
        layer.insertBefore(addToClassTree(tree, child, classPath + '/' + child), divider);
      }
    });
    if (divider === layer.firstChild) {
      layer.removeChild(divider);
    }
    pathElem.appendChild(nested);
    // Event for opening accordion
    pathElem.firstChild.nextSibling.addEventListener('click', navItemActivate);
  } else {
    pathElem.removeChild(pathElem.firstChild.nextSibling);
  }
  // Event for making query
  // pathElem.firstChild.nextSibling.addEventListener('onmouseover', turnOffSearch);
  const pageElement = document.getElementById('current-page');
  pathElem.onclick = function () {
    event.stopPropagation();
    if (!pageElement) {	
      redirectKeyword(classPath);	
    } else {	
      searchOrgs(0, classPath);
    }	
  };

  return pathElem;
}

/**
 * Open accordion, close any other paths on current level
 */
function navItemActivate() {
  this.parentNode.setAttribute('class', 'mdc-list-item mdc-list-item--activated');
  this.firstChild.innerText = 'expand_more';
  const navMenu = this.parentNode.parentNode;
  navMenu.childNodes.forEach(child => {
    if (child.getAttribute('role') !== 'separator') {
      child.firstChild.nextSibling.dispatchEvent(new CustomEvent('close'));
    }
  });
  this.addEventListener('click', navItemDeactivate);
  this.addEventListener('close', navItemDeactivate);
  this.removeEventListener('click', navItemActivate);
  const nextPath = this.parentNode.nextSibling;
  const targetLayer = this.parentNode.lastChild.firstChild.firstChild;
  navMenu.insertBefore(targetLayer, nextPath); 
  event.stopPropagation();
}

/**
 * Close accordion, close any paths in decending levels
 */
function navItemDeactivate() {
  this.parentNode.setAttribute('class', 'mdc-list-item mdc-list-item');
  this.firstChild.innerText = 'chevron_right';
  this.parentNode.parentNode.childNodes.forEach(child => child.dispatchEvent(new CustomEvent('close')));
  const navMenu = this.parentNode.parentNode;
  const nextPath = this.parentNode.nextSibling;
  this.parentNode.lastChild.firstChild.appendChild(nextPath);
  this.addEventListener('click', navItemActivate);
  this.removeEventListener('click', navItemDeactivate);
  this.removeEventListener('close', navItemDeactivate);
}

function setUpResults() {
  addListenerResults();
  getClassifications();
  getResults();
  getLoginStatus();
  setUpNavbar();

}

function setUpDetailsPage() {
  addListener();
  getClassifications();
  loadOrg();
  getLoginStatus();
  setUpNavbar();

}

function setUpRecommendations() {
  setUpNavbar();
  addListener();
  getClassifications();
  getLoginStatus().then(loggedIn => {
    if (loggedIn) {
      getRecommendations();
    } else {
      alert('Log in to get personalized recommendations');
    }
  });
}

function loadOrg() {
  const url = new URL(window.location.href);
  const id = url.searchParams.get('id');
  const qs = '/org?' + updateQueryString('id', id);
  addOrgs(qs, 0);
}

function getResults() {
  const url = new URL(window.location.href);
  const keyword = url.searchParams.get('keyword');
  if (keyword) {
    searchOrgs(0, keyword);
  }
}

function setUpIndexpage() {
  addListener();
  getClassifications();
  setUpNavbar();
  getLoginStatus();
}

function setUpAboutPage(){
  addListener();
  getLoginStatus();
  setUpNavbar();
}

/**
 * Fetches the login status of user
 * if logged in, display logout button
 * if logged out, display button redirect to login page
 */
function getLoginStatus() {
  return fetch('/login').then(response => response.text()).then(link => {
    // if user is logged in, server sends the logout link
    if (link.includes('logout')) {
      // is logged in 
      const statusElement = document.getElementById('login-status');
      statusElement.innerText = 'Hello!';
      const logoutElement = document.getElementById('login-link');
      if (!link.includes('http')) {
        logoutElement.href = link;
      } else {
        logoutElement.href = 'https://mit-step-2020.wl.r.appspot.com/_ah/logout?continue=https://mit-step-2020.wl.r.appspot.com/';
      }
      logoutElement.innerText = 'Logout';
      const loginIcon = document.getElementById('loginIcon')
      loginIcon.href = link;
      return 1;
    } else {
      // is logged out
      const statusElement = document.getElementById('login-status');
      statusElement.innerText = 'Please login  ';
      const loginElement = document.getElementById('login-link');
      loginElement.href = link;
      loginElement.innerText = 'Login';
      const loginIcon = document.getElementById('loginIcon')
      loginIcon.href = link;
      return 0;
    }
  });
}

function toggleSearch() {
  const status = document.getElementById("results-search").style.display;
  if (status === 'block') {
    closeSearch();
  } else {
    openSearch();
  }
}

/**  Open the search box */
function openSearch() {
  addCloseListener();
  document.getElementById("myOverlay").style.display = "block";
}

/** Close the search box */
function closeSearch() {
  document.getElementById("myOverlay").style.display = "none";
}

function accordion(){
  var acc = document.getElementsByClassName("accordion");
  var i;

  for (i = 0; i < acc.length; i++) {
    acc[i].addEventListener("click", function() {
      this.classList.toggle("active");
      var panel = this.nextElementSibling;
      if (panel.style.maxHeight) {
        panel.style.maxHeight = null;
      } else {
        panel.style.maxHeight = panel.scrollHeight + "px";
      }
    });
  }
}

function toggleHamburger() {
  const status = document.getElementById("drawer").style.display;
  if (status === 'block') {
    closeHamburger();
  } else {
    openHamburger();
  }
}

function openHamburger() {
  document.getElementById("drawer").style.display = "block";
  document.getElementById("drawer").style.zIndex="11";

  document.addEventListener('click', event => {
    if (event.target.id !== 'hamburger') {
      closeHamburger();
    }
  });
}

function closeHamburger() {  
  document.getElementById("drawer").style.display = "none";
  document.removeEventListener('click', event => {
    if (event.target.id !== 'hamburger') {
      closeHamburger();
    }
  });
}

function toggleLoginStatus() {
  const status = document.getElementById("login-drawer").style.display;
  if (status === 'block') {
    closeLoginStatus();
  } else {
    openLoginStatus();
  }
}

function openLoginStatus() {
  document.getElementById("login-drawer").style.display = "block";
  document.getElementById("login-drawer").style.zIndex="11";

  document.addEventListener('click', event => {
    if (event.target.id !== 'loginIcon') {
      closeLoginStatus();
    }
  });
}

function closeLoginStatus() {  
  document.getElementById("login-drawer").style.display = "none";
  document.removeEventListener('click', event => {
    if (event.target.id !== 'loginIcon') {
      closeLoginStatus();
    }
  });
}


function getRecommendations() {
  const statusElement = document.getElementById('login-status');
  const contentElement = document.getElementById('recommended-orgs');
  
  fetch('/recommend').then(response => response.json()).then(text => {
    const rated = text[0];
    const recommended = text[1];

    if (rated.length == 0) {
      alert("Please rate some organizations first");
      return;
    }
    const aboutElement = document.createElement('p');
    aboutElement.innerText = 'Because you liked: ';
    contentElement.appendChild(aboutElement);
    
    const listElement = document.createElement('ul');
    rated.forEach(org => {
      const ratedOrg = document.createElement("span");
      ratedOrg.setAttribute("class", "mdc-chip");
      ratedOrg.appendChild(getOrgNameAndId(org));
      listElement.appendChild(ratedOrg);
    });
    contentElement.appendChild(listElement)

    const textElement = document.createElement('p');
    textElement.innerText = 'You might also be interested in: ';
    contentElement.appendChild(textElement);

    const liElement = document.createElement('ul');
    recommended.forEach(org => {
      const reccomendOrg = document.createElement("span");
      reccomendOrg.setAttribute("class", "mdc-chip");
      reccomendOrg.appendChild(getOrgNameAndId(org));
      reccomendOrg.appendChild(getOrgNameAndId(org));
      liElement.appendChild(reccomendOrg);
    });
    contentElement.appendChild(liElement);
  });
}

function getOrgNameAndId(org) {
  const listElement = document.createElement('li');
  const nameElement = document.createElement('a');
  const redirect = '/organization.html?' + updateQueryString('id', org.index);
  nameElement.setAttribute('href', redirect);
  nameElement.innerText = org.name;
  listElement.appendChild(nameElement);
  return listElement;
}


function setUpNavbar() {
  document.getElementById("top-nav").innerHTML="<header class='mdc-top-app-bar' id='app-bar' style=\"left:0%\"><div class='mdc-top-app-bar__row'><section class='mdc-top-app-bar__section mdc-top-app-bar__section--align-start'><button id='hamburger' title='menu' onclick='toggleHamburger();' class='material-icons mdc-top-app-bar__navigation-icon mdc-icon-button' aria-label='Open navigation menu'>menu</button><a href='/index.html'><button title='home' class='material-icons mdc-top-app-bar__action-item mdc-icon-button' aria-label='Home'>home</button></a> <span class='mdc-top-app-bar__title'>Nonprofit Finder</span></section><section class='mdc-top-app-bar__section mdc-top-app-bar__section--align-end' role='toolbar'><button class='material-icons mdc-top-app-bar__action-item mdc-icon-button' title='search by keyword' aria-label='Search' onclick='toggleSearch();'>search</button><button id='loginIcon' title='login status' onclick='toggleLoginStatus();' class='material-icons mdc-top-app-bar__action-item mdc-icon-button' aria-label='Options'>account_circle</button></section></div></header>";

  // document.getElementById("myOverlay").innerHTML="<span class='closebtn material-icons' onclick='closeSearch()' title='Close Overlay'><span class='material-icons'>clear</span></span><div id=’results-search’><input type=’text’ id=’keyword’ placeholder=’Search by keyword’><span class=’material-icons’ onclick=’search();’>search</span> </div>";

}

function addJquery(){
    var script = document.createElement("SCRIPT");
    script.src = 'https://code.jquery.com/jquery-3.5.1.min.js';
    script.integrity="sha256-9/aliU8dGd2tb6OSsuzixeV4y/faTqgFtohetphbbj0=";
    script.crossorigin="anonymous";
    script.type = 'text/javascript';
    document.getElementsByTagName("head")[0].appendChild(script);
}

/**
 * Checks if current user is admin
 * Redirects invalid users to home page
 */
function loadAdminPage() {
  setUpNavbar();
  addListener();
  getLoginStatus();
  const qs = "/admin";
  fetch(qs).then(response => response.json()).then(adminText => {
    if (adminText['user'] === 'Admin') {
      setUpForAdmin(adminText);
    } else { 
      alert("You are unauthorized for this page");
      window.location.replace("/index.html");
    }
  });
}

/**
 * Sets up UI on page for admin
 * Adds functionality for dynamically selecting action items
 */
function setUpForAdmin(adminText) {
  // admin UI to page
  document.getElementById('navActionItems').style.display = 'block';
  document.getElementById('uploadOrgs').innerHTML = adminText['uploadCSV'];
  document.getElementById('quality').innerHTML = adminText['compareQuality'];
  document.getElementById('similarity').innerHTML = adminText['similarityWorkFlow'];
  document.getElementById('filter').innerHTML = adminText['filterWorkFlow'];
  // Hide items
  hideWorkFlows();
  // Add click functionality to display desired UI
  document.getElementById('actionItems').childNodes.forEach(child => {
    child.addEventListener('click', function() {
      hideWorkFlows();
      this.setAttribute('class', 'mdc-list-item mdc-list-item--activated');
      document.getElementById(this.getAttribute("value")).style.display = "block";
    });
  })
  getUploads(); 
}

// Hides unselected UI
function hideWorkFlows() {
  document.getElementById('uploadOrgs').style.display = "none";
  document.getElementById('quality').style.display = "none";
  document.getElementById('similarity').style.display = "none";
  document.getElementById('filter').style.display = "none";
  document.getElementById('uploadOrgsItem').setAttribute('class', 'mdc-list-item mdc-list-item')
  document.getElementById('qualityItem').setAttribute('class', 'mdc-list-item mdc-list-item')
  document.getElementById('similarityItem').setAttribute('class', 'mdc-list-item mdc-list-item')
  document.getElementById('filterItem').setAttribute('class', 'mdc-list-item mdc-list-item')
}

function getUploads() {
  const qs = "/verify";
  fetch(qs).then(response => response.json()).then(comparisonMap => {
    const submission = document.getElementById('submission');
    const comparisons = document.getElementById('comparisons');
    submission.appendChild(getOrgUploadHtmlDescription(comparisonMap["submission"][0], true));
    comparisonMap["similar"].forEach( org => comparisons.appendChild(getOrgUploadHtmlDescription(org, false)));
  });
}

/**
 * Creates list element from org
 */
function getOrgUploadHtmlDescription(org, submission) {
  const orgElement = document.createElement('div');
  orgElement.setAttribute("class", "mdc-card");
  // org name
  const nameElement = document.createElement('h3');
  nameElement.setAttribute("id", "org-name");
  nameElement.innerText = org.name;
  orgElement.appendChild(nameElement);
  // org link
  const linkElement = document.createElement('a');
  linkElement.setAttribute("id", "org-link");
  linkElement.setAttribute('href', 'https://' + org.link);
  linkElement.setAttribute('target', '_blank');
  linkElement.innerText = 'https://' + org.link;
  orgElement.appendChild(linkElement);
  // about
  const aboutElement = document.createElement('p');
  aboutElement.setAttribute("id", "about");
  aboutElement.innerText = org.about;
  orgElement.appendChild(aboutElement);
  
  if (submission) {
    const approve = document.createElement('button');
    approve.onclick = function() { sendUploadDecision("approve", org.id); }
    approve.innerText = 'Approve';
    const discard = document.createElement('button');
    discard.onclick = function() { sendUploadDecision("discard", org.id); }
    discard.innerText = 'Discard';

    orgElement.appendChild(approve);
    orgElement.appendChild(discard);
  }

  return orgElement;
}

function sendUploadDecision(decision, id) { 
  const params = "do=" + decision + '&id=' + id;
  fetch('/verify?' + params, {method: 'POST'}).then(response => {
    alert("Request complete! response:", response);
    location.reload();
  });
}
