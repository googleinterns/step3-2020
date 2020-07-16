/**
 * Add the searched organizations by keyword
 */
function searchOrgs(page) {
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
  removeOrgs();
  const keyword = document.getElementById('keyword').value;
  const qs = '/sql?' + updateQueryString('keyword', keyword) + '&' + updateQueryString('page', pageElement.innerText);
  addTitle(keyword);
  addPagination();
  addOrgs(qs);
}

function addPagination() {
  document.getElementById('pagination').style.display = 'inline-block';
}

function addOrgs(qs) {
  fetch(qs).then(response => response.json()).then(text => {
    console.log(text);
    const orgsContainer = document.getElementById('existing-organizations');
    text.forEach(entry => {
      orgsContainer.appendChild(getOrgAsHtmlDescription(entry));
    });
  });
}
 
function removeOrgs() {
  // remove previously displayed similar organizations
  var existingOrgs = document.getElementById('existing-organizations');
  while (existingOrgs.firstChild) {
    existingOrgs.removeChild(existingOrgs.firstChild);
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
function getOrgAsHtmlDescription(org) {
  const orgElement = document.createElement('div');
  orgElement.setAttribute("class", "mdc-card");
  //org name
  const nameElement = document.createElement('a');
  nameElement.setAttribute("id", "org-name");
  nameElement.setAttribute('href', 'https://' + org.link);
  nameElement.setAttribute('target', '_blank');
  nameElement.innerText = org.name;
  orgElement.appendChild(nameElement);
  //about
  const aboutElement = document.createElement('p');
  aboutElement.setAttribute("id", "about");
  aboutElement.innerText = org.about;
  orgElement.appendChild(aboutElement);
  // like this in chip format
  const neighborElement = document.createElement('p');
  neighborElement.setAttribute("id", "like-this");
  neighborElement.innerText = 'Like this: '
  const chipElement = document.createElement("div");
  chipElement.setAttribute("class", "mdc-chip-set");
  const org1 = document.createElement("span");
  org1.setAttribute("class", "mdc-chip");
  org1.innerText = org.neighbor1;
  neighborElement.appendChild(org1);
  const org2 = document.createElement("span");
  org2.setAttribute("class", "mdc-chip");
  org2.innerText = org.neighbor2;
  neighborElement.appendChild(org2);
  const org3 = document.createElement("span");
  org3.setAttribute("class", "mdc-chip");
  org3.innerText = org.neighbor3;
  neighborElement.appendChild(org3);
  const org4 = document.createElement("span");
  org4.setAttribute("class", "mdc-chip");
  org4.innerText = org.neighbor4;
  neighborElement.appendChild(org4);
  orgElement.appendChild(neighborElement);

  return orgElement;
}
 
function addListener() {
  const inputBox = document.getElementById('keyword');
  inputBox.addEventListener('keyup', function(event) {
      if (event.key === 'Enter') {
        searchOrgs(0);
      }
  });
}
 
function addTitle(keyword) {
  const element = document.getElementById('results-title');
  element.innerText = 'Results for [' + keyword + ']: ';
}

function getClassifications() {
  const qs = '/data';
  const classDiv = document.getElementById("roots");
  fetch(qs).then(response => response.json()).then(tree => {
    console.log(tree);
    tree.roots.forEach(root=> {
      classDiv.appendChild(addToClassTree(tree, root, root));
    });
    // var toggler = document.getElementsByClassName("uk-nav-parent-icon");
    // var i;
    // for (i = 0; i < toggler.length; i++) {
    //   toggler[i].addEventListener('click', function() {
    //     this.parentElement.querySelector('.uk-parent').classList.toggle("active");
    //     this.classList.toggle("caret-down");
    //   });
    // }
  }); 
}

function addToClassTree(tree, parent, classPath) {
  const parentElem = document.createElement('li');
  parentElem.setAttribute('value', classPath);
  if (!(tree[parent].length === 0)) {
    const icon = document.createElement('a');
    icon.innerText = parent;
    icon.href ="#";
    parentElem.appendChild(icon);
    parentElem.setAttribute('class', 'uk-parent');
    const nested = document.createElement('ul');
    nested.setAttribute('class', 'uk-nav-sub');
    tree[parent].forEach(child => nested.appendChild(addToClassTree(tree, child, classPath + "/" + child)));
    parentElem.appendChild(nested);
  } else {
    parentElem.innerText = parent;
    parentElem.addEventListener('click', function() {
      const pageElement = document.getElementById('current-page');
      const qs = '/sql?' + updateQueryString('keyword', classPath) + '&' + updateQueryString('page', pageElement.innerText);
      removeOrgs();
      addTitle(classPath);
      addPagination();
      addOrgs(qs);
    });
  }
  return parentElem;
}

function setUpPrototype() {
  addListener();
  getClassifications();
}

/**  Open the search box */
function openSearch() {
  document.getElementById("myOverlay").style.display = "block";
}

/** Close the search box */
function closeSearch() {
  document.getElementById("myOverlay").style.display = "none";
}

function getUploads() {
  const qs = "/verify";
  fetch(qs).then(response => response.json()).then(comparisonMap => {
    console.log(comparisonMap);
    const orgsContainer = document.getElementById('submission');
    orgsContainer.appendChild(getOrgUploadHtmlDescription(comparisonMap["Comparison_1"][0]));
  });
}



/**
 * Creates list element from org
 */
function getOrgUploadHtmlDescription(org) {
  const orgElement = document.createElement('div');
  orgElement.setAttribute("class", "mdc-card");
  //org name
  const nameElement = document.createElement('h3');
  nameElement.setAttribute("id", "org-name");
  nameElement.innerText = org.name;
  orgElement.appendChild(nameElement);
  //org link
  const linkElement = document.createElement('a');
  linkElement.setAttribute("id", "org-link");
  linkElement.setAttribute('href', 'https://' + org.link);
  linkElement.setAttribute('target', '_blank');
  linkElement.innerText = 'https://' + org.link;
  orgElement.appendChild(linkElement);
  //about
  const aboutElement = document.createElement('p');
  aboutElement.setAttribute("id", "about");
  aboutElement.innerText = org.about;
  orgElement.appendChild(aboutElement);

  const approve = document.createElement('button');
  approve.onclick = function() { sendUploadDecision("approve"); }
  approve.innerText = 'Approve';
  const discard = document.createElement('button');
  discard.onclick = function() { sendUploadDecision("discard"); }
  discard.innerText = 'Discard';

  orgElement.appendChild(approve);
  orgElement.appendChild(discard);
  return orgElement;
}

function sendUploadDecision(decision) { 
  fetch("/verify", {
    method: "POST", 
    body: "do:" + decision
  }).then(res => {
    console.log("Request complete! response:", res);
  });
}
